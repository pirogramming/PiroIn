const DEFAULT_RETRY_DELAY = 3000;

function buildSseHeaders(headers = {}) {
    const token = localStorage.getItem('token');
    const authHeader = token ? { Authorization: `Bearer ${token}` } : {};

    return {
        Accept: 'text/event-stream',
        'Cache-Control': 'no-cache',
        ...authHeader,
        ...headers,
    };
}

function parseSseData(rawData) {
    if (!rawData) {
        return null;
    }

    try {
        return JSON.parse(rawData);
    } catch {
        return rawData;
    }
}

export function parseSseMessage(rawMessage) {
    const lines = rawMessage.split('\n');
    let event = 'message';
    let id = null;
    let retry = null;
    const dataLines = [];

    lines.forEach((line) => {
        if (!line || line.startsWith(':')) {
            return;
        }

        const separatorIndex = line.indexOf(':');
        const field = separatorIndex === -1 ? line : line.slice(0, separatorIndex);
        let value = separatorIndex === -1 ? '' : line.slice(separatorIndex + 1);

        if (value.startsWith(' ')) {
            value = value.slice(1);
        }

        if (field === 'event') {
            event = value;
        } else if (field === 'data') {
            dataLines.push(value);
        } else if (field === 'id') {
            id = value;
        } else if (field === 'retry') {
            const retryValue = Number(value);
            retry = Number.isNaN(retryValue) ? null : retryValue;
        }
    });

    const rawData = dataLines.join('\n');

    return {
        event,
        data: parseSseData(rawData),
        rawData,
        id,
        retry,
    };
}

async function readSseStream(stream, onEvent, signal) {
    const reader = stream.getReader();
    const decoder = new TextDecoder();
    let buffer = '';

    while (!signal.aborted) {
        const { done, value } = await reader.read();

        if (done) {
            break;
        }

        buffer += decoder.decode(value, { stream: true }).replace(/\r\n/g, '\n');

        let messageEndIndex = buffer.indexOf('\n\n');
        while (messageEndIndex !== -1) {
            const rawMessage = buffer.slice(0, messageEndIndex);
            buffer = buffer.slice(messageEndIndex + 2);

            if (rawMessage.trim()) {
                onEvent(parseSseMessage(rawMessage));
            }

            messageEndIndex = buffer.indexOf('\n\n');
        }
    }

    buffer += decoder.decode();

    if (buffer.trim()) {
        onEvent(parseSseMessage(buffer));
    }
}

export function subscribeToSse(
    url,
    {
        headers,
        retry = true,
        retryDelay = DEFAULT_RETRY_DELAY,
        onOpen,
        onEvent,
        onError,
    } = {}
) {
    let closed = false;
    let retryTimer = null;
    let controller = null;

    const clearRetryTimer = () => {
        if (retryTimer) {
            clearTimeout(retryTimer);
            retryTimer = null;
        }
    };

    const scheduleReconnect = () => {
        if (closed || !retry) {
            return;
        }

        clearRetryTimer();
        retryTimer = setTimeout(connect, retryDelay);
    };

    const connect = async () => {
        controller = new AbortController();

        try {
            const response = await fetch(url, {
                method: 'GET',
                headers: buildSseHeaders(headers),
                signal: controller.signal,
            });

            if (!response.ok) {
                const error = new Error(`SSE connection failed: ${response.status}`);
                error.status = response.status;
                error.retryable = response.status === 429 || response.status >= 500;
                throw error;
            }
            if (!response.body) {
                throw new Error('SSE response body is empty.');
            }

            onOpen?.(response);
            await readSseStream(response.body, (message) => {
                if (!closed) {
                    onEvent?.(message);
                }
            }, controller.signal);

            scheduleReconnect();
        } catch (error) {
            if (closed || error.name === 'AbortError') {
                return;
            }

            onError?.(error);
            if (error.retryable !== false) {
                scheduleReconnect();
            }
        }
    };

    connect();

    return () => {
        closed = true;
        clearRetryTimer();
        controller?.abort();
    };
}

export function subscribeQuestionEvents(sessionId, handlers = {}) {
    if (!sessionId) {
        return () => {};
    }

    return subscribeToSse(`/api/sessions/${sessionId}/questions/events`, handlers);
}
