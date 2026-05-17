import { useState } from 'react';
import styles from './QnAListPage.module.css';
import { FiChevronLeft, FiChevronRight, FiMessageSquare } from 'react-icons/fi';

const CommentImoji = () => (<svg xmlns="http://www.w3.org/2000/svg" width="15" height="12" viewBox="0 0 15 12" fill="none">
    <path d="M3.95833 4.24177C3.38304 4.24177 2.91667 4.70814 2.91667 5.28344C2.91667 5.85874 3.38304 6.32511 3.95833 6.32511C4.53363 6.32511 5 5.85874 5 5.28344C5 4.70814 4.53363 4.24177 3.95833 4.24177Z" fill="
#555555"/>
    <path d="M7.29167 4.24177C6.71637 4.24177 6.25 4.70814 6.25 5.28344C6.25 5.85874 6.71637 6.32511 7.29167 6.32511C7.86696 6.32511 8.33333 5.85874 8.33333 5.28344C8.33333 4.70814 7.86696 4.24177 7.29167 4.24177Z" fill="
#555555"/>
    <path d="M9.58333 5.28344C9.58333 4.70814 10.0497 4.24177 10.625 4.24177C11.2003 4.24177 11.6667 4.70814 11.6667 5.28344C11.6667 5.85874 11.2003 6.32511 10.625 6.32511C10.0497 6.32511 9.58333 5.85874 9.58333 5.28344Z" fill="
#555555"/>
    <path fillrule="evenodd" cliprule="evenodd" d="M10.7089 0.152431C8.46378 -0.0390355 6.20689 -0.0501471 3.95995 0.119203L3.79892 0.13134C1.65617 0.292838 0 2.07853 0 4.22736V11.3251C0 11.545 0.115555 11.7487 0.30429 11.8616C0.493026 11.9744 0.727171 11.9797 0.920865 11.8756L4.17986 10.1242C4.33145 10.0427 4.50087 10.0001 4.67297 10.0001H12.1533C13.0967 10.0001 13.9052 9.32561 14.0743 8.39749C14.417 6.51646 14.4441 4.59149 14.1544 2.70156L14.0691 2.14462C13.9136 1.13067 13.0841 0.354988 12.0621 0.267824L10.7089 0.152431ZM4.0539 1.36567C6.23419 1.20134 8.42415 1.21212 10.6027 1.39791L11.9558 1.5133C12.4028 1.55142 12.7655 1.89062 12.8335 2.33402L12.9189 2.89095C13.1875 4.64369 13.1624 6.42893 12.8446 8.17342C12.7837 8.5074 12.4928 8.75011 12.1533 8.75011H4.67297C4.29435 8.75011 3.92163 8.84391 3.58813 9.02315L1.25 10.2797V4.22736C1.25 2.73244 2.40218 1.49016 3.89287 1.3778L4.0539 1.36567Z" fill="
#555555"/>
</svg>);

const MeCuriousToo = () => (<svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 15 15" fill="none">
    <g clip-path="url(#clip0_245_1198)">
        <path d="M8.35213 0.0807455C8.37068 0.092755 8.38922 0.104764 8.40832 0.117138C8.42657 0.128543 8.44481 0.139948 8.4636 0.151699C8.64017 0.281158 8.76296 0.488324 8.79918 0.704012C8.8152 0.869963 8.81209 1.03635 8.81043 1.20287C8.81053 1.25859 8.8107 1.3143 8.81095 1.37002C8.81131 1.48934 8.81113 1.60864 8.81051 1.72796C8.80954 1.91671 8.80981 2.10544 8.81025 2.29419C8.81081 2.59266 8.81054 2.89111 8.80992 3.18958C8.80882 3.72439 8.8087 4.25919 8.80948 4.794C8.80972 4.98158 8.80946 5.16914 8.80882 5.35671C8.80849 5.47333 8.80852 5.58995 8.80864 5.70657C8.80862 5.76058 8.80844 5.81458 8.8081 5.86859C8.80766 5.9424 8.80776 6.0162 8.80798 6.09001C8.80772 6.11129 8.80746 6.13256 8.80719 6.15449C8.80844 6.30341 8.83926 6.40298 8.90637 6.53315C8.97989 6.59399 9.01575 6.59207 9.11145 6.58809C9.21562 6.5577 9.25042 6.53073 9.31653 6.44526C9.33407 6.34935 9.33407 6.34935 9.33114 6.23981C9.3313 6.21912 9.33146 6.19843 9.33162 6.17712C9.33201 6.10778 9.33156 6.03847 9.33113 5.96912C9.33124 5.91941 9.3314 5.86969 9.33162 5.81997C9.33197 5.71287 9.33195 5.60577 9.33166 5.49866C9.3312 5.3291 9.3316 5.15954 9.33213 4.98998C9.33303 4.66035 9.33307 4.33072 9.33302 4.00109C9.33298 3.58226 9.33328 3.16343 9.33444 2.7446C9.33488 2.57627 9.33487 2.40795 9.33451 2.23962C9.33437 2.13451 9.3346 2.02941 9.33491 1.92431C9.33498 1.87583 9.3349 1.82736 9.33466 1.77888C9.33308 1.44548 9.33314 1.11241 9.55697 0.843265C9.75752 0.643587 9.96154 0.572527 10.2375 0.547436C10.528 0.572914 10.731 0.682029 10.9352 0.886181C11.1259 1.18582 11.1202 1.46817 11.1182 1.81192C11.1183 1.86768 11.1185 1.92343 11.1187 1.97918C11.1191 2.09876 11.1191 2.21832 11.1188 2.3379C11.1183 2.527 11.1187 2.71609 11.1192 2.9052C11.1201 3.2723 11.1202 3.6394 11.1201 4.0065C11.1201 4.47421 11.1204 4.94191 11.1215 5.40961C11.122 5.59758 11.122 5.78555 11.1216 5.97352C11.1215 6.09036 11.1217 6.2072 11.122 6.32404C11.1221 6.37821 11.122 6.43237 11.1218 6.48653C11.1215 6.56048 11.1217 6.63442 11.122 6.70837C11.1218 6.72977 11.1216 6.75118 11.1214 6.77323C11.1228 6.90957 11.1473 7.00401 11.2208 7.11909C11.3169 7.16922 11.3691 7.18345 11.4753 7.16121C11.5602 7.10847 11.5866 7.06172 11.631 6.97261C11.6439 6.8921 11.6439 6.8921 11.6419 6.8057C11.6421 6.7724 11.6423 6.73909 11.6425 6.70478C11.6422 6.66851 11.642 6.63224 11.6418 6.59487C11.642 6.53591 11.6421 6.47694 11.6423 6.41797C11.6424 6.35471 11.6423 6.29145 11.6421 6.22818C11.6418 6.09604 11.6421 5.9639 11.6426 5.83176C11.6437 5.50345 11.6438 5.17513 11.6438 4.84681C11.6437 4.5918 11.6439 4.33678 11.6449 4.08177C11.6454 3.95064 11.6454 3.81953 11.6449 3.68841C11.6447 3.58724 11.6453 3.48608 11.6457 3.38492C11.6454 3.34895 11.6451 3.31297 11.6448 3.27591C11.6472 2.99064 11.7036 2.74725 11.9038 2.53505C12.1126 2.34563 12.3424 2.2734 12.6234 2.28511C12.8653 2.31862 13.0875 2.42787 13.2441 2.61653C13.4113 2.83987 13.4309 3.08842 13.4291 3.35842C13.4292 3.39191 13.4293 3.4254 13.4294 3.45991C13.4297 3.5717 13.4293 3.68348 13.429 3.79527C13.429 3.87561 13.4292 3.95594 13.4293 4.03628C13.4296 4.23164 13.4294 4.42699 13.4291 4.62234C13.4286 4.85026 13.4287 5.07818 13.4288 5.30609C13.4289 5.71317 13.4285 6.12025 13.4279 6.52733C13.4274 6.92142 13.4272 7.3155 13.4273 7.70959C13.4275 8.1394 13.4275 8.56921 13.4272 8.99902C13.4271 9.04498 13.4271 9.09094 13.4271 9.1369C13.427 9.17081 13.427 9.17081 13.427 9.2054C13.4269 9.36385 13.4269 9.52229 13.427 9.68074C13.427 9.87432 13.4269 10.0679 13.4265 10.2615C13.4262 10.36 13.4262 10.4586 13.4263 10.5571C13.4267 11.0664 13.4169 11.5621 13.2936 12.0593C13.2873 12.085 13.281 12.1108 13.2745 12.1374C13.1221 12.7373 12.8388 13.2968 12.4117 13.7453C12.3329 13.8273 12.3329 13.8273 12.2619 13.9175C11.804 14.4502 11.0361 14.7373 10.3712 14.8883C10.3515 14.8928 10.3317 14.8973 10.3114 14.902C9.87696 14.9957 9.45139 15.0107 9.00891 15.0091C8.96402 15.009 8.96402 15.009 8.91822 15.0089C8.48863 15.0077 8.06872 14.9995 7.64661 14.9121C7.62075 14.9069 7.5949 14.9017 7.56826 14.8964C6.85195 14.7492 6.18979 14.4926 5.59582 14.0624C5.57109 14.0449 5.54635 14.0273 5.52087 14.0092C5.35393 13.8834 5.20493 13.7378 5.05566 13.5919C5.03575 13.5727 5.01583 13.5536 4.99531 13.5338C4.67755 13.2171 4.43762 12.8296 4.19382 12.4554C4.14755 12.3844 4.10113 12.3134 4.05467 12.2425C3.98781 12.1405 3.921 12.0384 3.85422 11.9363C3.6767 11.665 3.49824 11.3943 3.31982 11.1236C3.30195 11.0965 3.28407 11.0693 3.26565 11.0414C3.0424 10.7025 2.81835 10.3642 2.59296 10.0268C2.49201 9.87565 2.39121 9.72442 2.29045 9.57316C2.26767 9.53898 2.24488 9.5048 2.22209 9.47062C2.15008 9.36264 2.07842 9.25442 2.00696 9.14607C1.98993 9.12056 1.9729 9.09506 1.95536 9.06878C1.70984 8.69467 1.51004 8.32083 1.5968 7.86193C1.6755 7.57085 1.82995 7.34881 2.0802 7.17768C2.40717 7.01888 2.70771 7.02574 3.04688 7.14106C3.46078 7.3133 3.71301 7.75095 3.96252 8.10218C4.0639 8.24376 4.16794 8.38339 4.27171 8.52322C4.37488 8.66264 4.47629 8.80324 4.57699 8.94445C4.59049 8.96337 4.60399 8.98228 4.6179 9.00177C4.65092 9.04803 4.68392 9.0943 4.71692 9.14057C4.71696 9.1031 4.717 9.06562 4.71705 9.02701C4.7181 8.13679 4.71974 7.24657 4.72213 6.35635C4.72242 6.24671 4.72271 6.13707 4.723 6.02742C4.72308 5.99468 4.72308 5.99468 4.72317 5.96128C4.72408 5.60816 4.72458 5.25503 4.72492 4.90191C4.72529 4.5394 4.72609 4.17689 4.72731 3.81438C4.72804 3.5908 4.72844 3.36724 4.72839 3.14366C4.72839 2.97204 4.729 2.80042 4.72984 2.6288C4.73009 2.55853 4.73014 2.48826 4.72998 2.41799C4.72978 2.32187 4.73031 2.22577 4.73101 2.12964C4.73078 2.102 4.73056 2.07435 4.73032 2.04587C4.73322 1.82032 4.79496 1.63493 4.91284 1.44282C5.10958 1.2514 5.32523 1.13496 5.60315 1.13154C5.85841 1.13619 6.06704 1.2096 6.25409 1.38606C6.52217 1.68149 6.51268 2.02471 6.51151 2.40571C6.51159 2.44973 6.5117 2.49374 6.51183 2.53776C6.51207 2.63196 6.51213 2.72616 6.51207 2.82036C6.51198 2.96933 6.51241 3.11829 6.51292 3.26725C6.51429 3.69064 6.515 4.11403 6.51519 4.53742C6.5153 4.77169 6.51582 5.00596 6.5168 5.24023C6.51739 5.38833 6.51746 5.53643 6.51714 5.68453C6.51707 5.7765 6.51748 5.86846 6.51802 5.96043C6.51818 6.00309 6.51814 6.04575 6.51788 6.08841C6.51755 6.14659 6.51801 6.20479 6.51851 6.26297C6.51855 6.29554 6.51858 6.3281 6.51861 6.36166C6.5368 6.46499 6.56857 6.49887 6.65051 6.56245C6.75846 6.5969 6.80527 6.60156 6.90869 6.5533C6.97539 6.50925 6.97539 6.50925 7.00207 6.44526C7.00504 6.37225 7.00623 6.30006 7.00612 6.22704C7.0062 6.20426 7.00628 6.18149 7.00637 6.15803C7.00661 6.0813 7.00665 6.00457 7.0067 5.92783C7.00683 5.87298 7.00698 5.81812 7.00715 5.76326C7.00748 5.64487 7.00772 5.52648 7.00791 5.40809C7.00822 5.22068 7.00882 5.03327 7.00947 4.84587C7.01128 4.31285 7.01286 3.77984 7.01383 3.24682C7.01437 2.95264 7.01522 2.65845 7.01641 2.36427C7.01703 2.20863 7.01748 2.053 7.01755 1.89737C7.01761 1.75073 7.01808 1.6041 7.01885 1.45746C7.01906 1.40385 7.01912 1.35023 7.01902 1.29662C7.0178 0.513443 7.0178 0.513443 7.29504 0.234325C7.58174 -0.0220407 8.00007 -0.110807 8.35213 0.0807455Z" fill="#09C410" />
    </g>
    <defs>
        <clipPath id="clip0_245_1198">
            <rect width="15" height="15" fill="white" />
        </clipPath>
    </defs>
</svg>);

const StaffCheck = () => (<svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 13 13" fill="none">
    <circle cx="3.9" cy="9.10019" r="3.9" fill="#09C410" />
    <circle cx="9.10019" cy="3.9" r="3.9" fill="#09C410" />
    <circle cx="3.9" cy="3.9" r="3.9" fill="#09C410" />
    <circle cx="9.10019" cy="9.10019" r="3.9" fill="#09C410" />
    <path fillrule="evenodd" cliprule="evenodd" d="M9.87698 4.12922C10.041 4.30151 10.041 4.58085 9.87698 4.75314L5.95698 8.87078C5.79296 9.04307 5.52704 9.04307 5.36302 8.87078L3.12302 6.51784C2.95899 6.34555 2.95899 6.06621 3.12302 5.89392C3.28704 5.72163 3.55296 5.72163 3.71698 5.89392L5.66 7.93491L9.28302 4.12922C9.44703 3.95693 9.71297 3.95693 9.87698 4.12922Z" fill="#F0FFF1" />
</svg>);

const SortBtn = () => (<svg xmlns="http://www.w3.org/2000/svg" width="11" height="6" viewBox="0 0 11 6" fill="none">
    <path fillrule="evenodd" cliprule="evenodd" d="M10.7456 0.23964C11.0848 0.55916 11.0848 1.0772 10.7456 1.39672L6.11407 5.76036C5.77493 6.07988 5.22507 6.07988 4.88593 5.76036L0.254355 1.39672C-0.0847849 1.0772 -0.0847849 0.55916 0.254355 0.23964C0.593494 -0.0798796 1.14335 -0.0798796 1.48249 0.23964L5.5 4.02473L9.51751 0.23964C9.85665 -0.07988 10.4065 -0.07988 10.7456 0.23964Z" fill="#555555" />
</svg>);

const OBtn = () => (<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none">
    <g clip-path="url(#clip0_245_1011)">
        <path d="M12 0C9.62663 0 7.30655 0.703788 5.33316 2.02236C3.35977 3.34094 1.8217 5.21509 0.913451 7.4078C0.00519943 9.60051 -0.232441 12.0133 0.230582 14.3411C0.693605 16.6689 1.83649 18.8071 3.51472 20.4853C5.19295 22.1635 7.33115 23.3064 9.65892 23.7694C11.9867 24.2324 14.3995 23.9948 16.5922 23.0866C18.7849 22.1783 20.6591 20.6402 21.9776 18.6668C23.2962 16.6935 24 14.3734 24 12C23.9966 8.81846 22.7312 5.76821 20.4815 3.51852C18.2318 1.26883 15.1815 0.00344108 12 0V0ZM12 21C10.22 21 8.47992 20.4722 6.99987 19.4832C5.51983 18.4943 4.36628 17.0887 3.68509 15.4442C3.0039 13.7996 2.82567 11.99 3.17294 10.2442C3.5202 8.49836 4.37737 6.89471 5.63604 5.63604C6.89472 4.37737 8.49836 3.5202 10.2442 3.17293C11.99 2.82567 13.7996 3.0039 15.4442 3.68508C17.0887 4.36627 18.4943 5.51983 19.4832 6.99987C20.4722 8.47991 21 10.22 21 12C20.9974 14.3861 20.0483 16.6738 18.361 18.361C16.6738 20.0483 14.3861 20.9974 12 21Z" fill="currentColor" />
    </g>
    <defs>
        <clipPath id="clip0_245_1011">
            <rect width="24" height="24" fill="white" />
        </clipPath>
    </defs>
</svg>);

const XBtn = () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="21" height="21" viewBox="0 0 21 21" fill="none">
        <path d="M1.25 19.25L19.25 1.25" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
        <path d="M1.25 1.25L19.25 19.25" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" />
    </svg>
);

const UNDERSTAND = ['이해했다', '성공했다'];

// 댓글 최대 표시 개수 (카드에서 항상 노출)
const MAX_VISIBLE_COMMENTS = 3;

// 질문 목록 데이터
const MOCK_QUESTIONS = [
    {
        id: 1,
        text: '벤브 어떻게 활성화 시켜요?',
        likes: 7,
        iLiked: false,    // 내가 좋아요 눌렀는지
        image: null,      // 첨부 이미지 없음
        comments: [],     // 댓글 없음
    },
    {
        id: 2,
        text: '오류났어요',
        likes: 7,
        iLiked: false,
        image: 'https://dora-guide.com/wp-content/uploads/2019/11/Visual-studio-code-%EC%84%A4%EC%B9%98-%EB%B0%8F-%EC%82%AC%EC%9A%A9%EB%B2%95.png',
        comments: [
            { id: 1, author: '운영진1', isStaff: true, content: '사진 참고하세요' },
            { id: 2, author: '작성자', isStaff: false, content: '감사합니다' },
            { id: 3, author: '익명1', isStaff: false, content: '감사합니다' },
        ],
    },
    {
        id: 3,
        text: '벤브 어떻게 활성화 시켜요?',
        likes: 7,
        iLiked: false,
        image: null,
        comments: [],
    },
    {
        id: 4,
        text: '벤브 어떻게 활성화 시켜요?',
        likes: 7,
        iLiked: false,
        image: null,
        comments: [],
    },
    {
        id: 5,
        text: '벤브 어떻게 활성화 시켜요?',
        likes: 7,
        iLiked: false,
        image: null,
        comments: [],
    },
];





function QnAListPage({
    sessionTitle = '1주차 화요일 오전 세션(HTML/CSS)',
    sessionId = 1,
    onBack,
    onCardClick, // 카드 클릭 시 상세 페이지 이동 (questionId를 인자로 받아)
}) {



    // 현재 보고 있는 이해도 체크 인덱스 (0 = '이해했다')
    const [understandIndex, setUnderstandIndex] = useState(0);

    // 저도 궁금해요 필터 켜져 있는지
    const [filterCurious, setFilterCurious] = useState(false);

    // 정렬 방식
    const [sortOrder, setSortOrder] = useState('정렬');

    // 정렬 드롭다운 열려 있는지
    const [showSortMenu, setShowSortMenu] = useState(false);

    // 질문 목록 (좋아요 토글 등 변경사항 반영하려고 state로 관리)
    const [questions, setQuestions] = useState(MOCK_QUESTIONS);

    // 내가 이해했는지 여부: true = O 누름, false = X 누름, null = 아직 안 누름
    const [myUnderstand, setMyUnderstand] = useState(null);

    // 댓글 입력창이 열려 있는 질문 id
    // null이면 아무 질문도 댓글 입력창이 안 열려 있는 상태
    // '댓글달기' 버튼을 누른 질문의 id가 들어옴
    const [commentOpenId, setCommentOpenId] = useState(null);

    // 각 질문별 댓글 입력창 텍스트 (객체로 관리: { 질문id: 입력된텍스트 })
    const [commentInputs, setCommentInputs] = useState({});

    // 새 질문 입력창 텍스트
    const [newQuestion, setNewQuestion] = useState('');

    // API 요청 중인지 여부 (true면 버튼 비활성화 → 중복 제출 방지)
    const [isSubmitting, setIsSubmitting] = useState(false);

    // 에러 메시지 (null이면 에러 없음)
    const [submitError, setSubmitError] = useState(null);


    // 이해도
    const goPrevUnderstand = () => {
        if (understandIndex > 0) setUnderstandIndex(prev => prev - 1);
    };
    const goNextUnderstand = () => {
        if (understandIndex < UNDERSTAND.length - 1) setUnderstandIndex(prev => prev + 1);
    };

    // 질문
    const toggleLike = (e, id) => {
        e.stopPropagation();

        setQuestions(prev =>
            prev.map(q =>
                q.id === id
                    ? {
                        ...q,
                        iLiked: !q.iLiked,
                        likes: q.iLiked ? q.likes - 1 : q.likes + 1,
                    }
                    : q
            )
        );
    };

    const toggleCommentInput = (e, questionId) => {
        e.stopPropagation();
        setCommentOpenId(prev => prev === questionId ? null : questionId);
    };

    const handleCommentChange = (questionId, value) => {
        setCommentInputs(prev => ({
            ...prev,
            [questionId]: value,
        }));
    };

    const handleCommentSubmit = (e, questionId) => {
        e.stopPropagation();

        const text = (commentInputs[questionId] || '').trim();
        if (!text) return;

        setQuestions(prev =>
            prev.map(q =>
                q.id === questionId
                    ? {
                        ...q,
                        comments: [
                            ...q.comments,
                            { id: Date.now(), author: '나', isStaff: false, content: text },
                        ],
                    }
                    : q
            )
        );
        setCommentInputs(prev => ({ ...prev, [questionId]: '' }));
        setCommentOpenId(null);
    };



    const handleNewQuestion = async () => {
        const text = newQuestion.trim();
        if (!text) return;

        setIsSubmitting(true);
        setSubmitError(null);

        try {
            setQuestions(prev => [
                { id: Date.now(), text, likes: 0, iLiked: false, image: null, comments: [] },
                ...prev,
            ]);
            setNewQuestion('');

        } catch (error) {
            console.error('질문 등록 실패:', error);
            setSubmitError('질문 등록에 실패했어요.');
        } finally {
            setIsSubmitting(false);
        }
    };




    const currentUnderstand = UNDERSTAND[understandIndex];

    // 저도 궁금해요 필터가 켜져 있으면 필터링
    const displayedQuestions = filterCurious
        ? questions.filter(q => q.iLiked)
        : questions;






    return (
        // 상단
        <div className={styles.page}>

            <h1 className={styles.title}>{sessionTitle}</h1>

            <div className={styles.filterRow}>
                <label className={styles.curiousLabel}>
                    <input
                        type="checkbox"
                        checked={filterCurious}
                        onChange={e => setFilterCurious(e.target.checked)}
                        className={styles.curiousCheckbox}
                    />
                    저도 궁금해요
                </label>

                <div className={styles.sortWrapper}>
                    <button
                        className={styles.sortBtn}
                        onClick={() => setShowSortMenu(prev => !prev)} // 토글
                    >
                        {sortOrder} <SortBtn />
                    </button>

                    {showSortMenu && (
                        <ul className={styles.sortMenu}>
                            {['기본', '최신순', '저도궁금해요순'].map(option => (
                                <li
                                    key={option}
                                    className={styles.sortOption}
                                    onClick={() => {
                                        setSortOrder(option);
                                        setShowSortMenu(false);
                                    }}
                                >
                                    {option}
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            </div>
            <hr className={styles.divider} />


            {/* 이해도 */}
            <div className={styles.understandBar}>

                <button className={styles.arrowBtn} onClick={goPrevUnderstand} disabled={understandIndex === 0}>
                    <FiChevronLeft size={30} />
                </button>
                <span className={styles.understandName}>
                    {currentUnderstand}
                    <span className={styles.understandCount}> (13/29)</span>

                </span>
                <button
                    className={`${styles.oxBtn} ${styles.oxO} ${myUnderstand === true ? styles.oxActive : ''}`}
                    onClick={() => setMyUnderstand(prev => prev === true ? null : true)}
                    title="이해했어요"
                >
                    <OBtn />
                </button>
                <button
                    className={`${styles.oxBtn} ${styles.oxX} ${myUnderstand === false ? styles.oxActive : ''}`}
                    onClick={() => setMyUnderstand(prev => prev === false ? null : false)}
                    title="모르겠어요"
                >
                    <XBtn />
                </button>
                <button className={styles.arrowBtn} onClick={goNextUnderstand} disabled={understandIndex === UNDERSTAND.length - 1}>
                    <FiChevronRight size={30} />
                </button>
            </div>


            {/* ── 질문 목록 ── */}
            <div className={styles.questionList}>
                {displayedQuestions.map(question => (
                    <div
                        key={question.id}
                        className={styles.questionCard}
                        onClick={() => onCardClick?.(question.id)}
                    >

                        <div className={styles.questionHeader}>
                            <span className={styles.qIcon}>Q.</span>
                            <span className={styles.questionText}>{question.text}</span>

                            <div className={styles.questionActions}>
                                <button
                                    className={`${styles.likeBtn} ${question.iLiked ? styles.liked : ''}`}
                                    onClick={e => toggleLike(e, question.id)}
                                >
                                    <MeCuriousToo />{question.likes}
                                </button>
                                <button
                                    className={styles.commentBtn}
                                    onClick={e => toggleCommentInput(e, question.id)}
                                >
                                    <CommentImoji />
                                    &nbsp;댓글달기
                                </button>
                            </div>
                        </div>
                        {question.image && (
                            <img
                                src={question.image}
                                alt="첨부 이미지"
                                className={styles.questionImage}
                                onClick={e => e.stopPropagation()}
                            />
                        )}

                        {question.comments.length > 0 && (
                            <div className={styles.commentPreview}>
                                {question.comments.slice(0, MAX_VISIBLE_COMMENTS).map(comment => (
                                    <div key={comment.id} className={styles.commentItem}>
                                        <span className={styles.commentAuthor}>
                                            {comment.author}
                                            {comment.isStaff && (
                                                <span className={styles.staffBadge}><StaffCheck /></span>
                                            )}
                                        </span>
                                        {/* 댓글 내용 */}
                                        <div className={styles.commentContent}>
                                            ↳ {comment.content}
                                        </div>
                                    </div>
                                ))}


                                {question.comments.length > MAX_VISIBLE_COMMENTS && (
                                    <span className={styles.commentMore}>
                                        외 {question.comments.length - MAX_VISIBLE_COMMENTS}개 댓글
                                    </span>
                                )}
                            </div>
                        )}
                        {commentOpenId === question.id && (
                            <div
                                className={styles.commentInputRow}
                                onClick={e => e.stopPropagation()}
                            >
                                <input
                                    className={styles.commentInput}
                                    placeholder="댓글을 입력해주세요..."
                                    value={commentInputs[question.id] || ''}
                                    onChange={e => handleCommentChange(question.id, e.target.value)}
                                    // 엔터 누르면 제출
                                    onKeyDown={e => {
                                        if (e.key === 'Enter') handleCommentSubmit(e, question.id);
                                    }}
                                    autoFocus
                                />
                                <button
                                    className={styles.submitBtn}
                                    onClick={e => handleCommentSubmit(e, question.id)}
                                >
                                    ↑
                                </button>
                            </div>
                        )}
                    </div>
                ))}
            </div>



            <div className={styles.newQuestionBar}>

                {submitError && (
                    <p className={styles.errorMsg}>{submitError}</p>
                )}

                <div className={styles.newQuestionInputRow}>
                    <span className={styles.newQuestionPlus}>+</span>
                    <input
                        className={styles.newQuestionInput}
                        placeholder="질문을 남겨주세요..."
                        value={newQuestion}
                        onChange={e => setNewQuestion(e.target.value)}
                        onKeyDown={e => {
                            if (e.key === 'Enter') handleNewQuestion();
                        }}
                        disabled={isSubmitting}
                    />
                    <button
                        className={styles.newQuestionSubmit}
                        onClick={handleNewQuestion}
                        disabled={isSubmitting}
                    >
                        {isSubmitting ? '⏳' : '↑'}
                    </button>
                </div>
            </div>

        </div>
    );
}

export default QnAListPage;