-- 커뮤니티 데모/발표용 목 데이터.
-- 기존 seed 유저(seed%@olma.local)는 전부 디자인 직군이라 직군/경력 필터 데모가 빈약하다.
-- 그래서 다양한 직군·경력을 가진 커뮤니티 전용 유저를 별도로 만들고 그 위에 콘텐츠를 쌓는다.
-- 카운트 컬럼(like_count/comment_count/report_count)은 마지막에 실제 행 수로 재계산해 정합성을 맞춘다.

-- ---------------------------------------------------------------------------
-- 1. 커뮤니티 데모 유저 10명 (직군/경력 다양화)
-- ---------------------------------------------------------------------------
INSERT INTO users (email, password, nickname, agreement_at, experience_level_id, job_category_id)
SELECT
    v.email,
    '$$SEED$$NOT_A_REAL_HASH_DO_NOT_LOGIN',
    v.nickname,
    now() - make_interval(days => v.days_ago),
    v.exp_id,
    jc.id
FROM (VALUES
    ('community1@olma.local',  '백엔드하는곰',   4, 'backend',               120),
    ('community2@olma.local',  '프론트새싹',     2, 'frontend',              110),
    ('community3@olma.local',  '픽셀장인',       3, 'web-uiux',              100),
    ('community4@olma.local',  '기획하는PO',     5, 'pm-po',                  95),
    ('community5@olma.local',  '광고비쓰는사람', 2, 'performance-marketing',  90),
    ('community6@olma.local',  '컷편집러',       3, 'video-editing',          80),
    ('community7@olma.local',  '앱UI뉴비',       1, 'app-uiux',               70),
    ('community8@olma.local',  '데이터굽는집',   4, 'data-engineering',       60),
    ('community9@olma.local',  '문장옮기는이',   3, 'en-ko-translation',      55),
    ('community10@olma.local', '서비스빌더',     2, 'service-planning',       50)
) AS v(email, nickname, exp_id, slug, days_ago)
LEFT JOIN job_categories jc ON jc.slug = v.slug;

-- ---------------------------------------------------------------------------
-- 2. 게시글 25개 (QNA / INFO / FREE). 작성자 프로필은 작성 시점 스냅샷으로 복사.
-- ---------------------------------------------------------------------------
INSERT INTO community_posts (
    user_id, category, title, content, status,
    author_job_category_id, author_job_category_name,
    author_experience_level_id, author_experience_level_label,
    created_at, updated_at
)
SELECT
    u.id, v.category, v.title, v.content, v.status,
    u.job_category_id, jc.name,
    u.experience_level_id, el.label,
    now() - make_interval(days => v.age_days),
    now() - make_interval(days => v.age_days)
FROM (VALUES
    -- QNA
    ('community1@olma.local',  'QNA',  '외주 백엔드 유지보수 월 단가 어느 정도가 적정할까요?', '레거시 스프링 서버 유지보수 겸 소규모 기능 추가 건입니다. 월 단위로 계약하려는데 시니어 기준 어느 선이 적정한지 다른 분들 경험이 궁금합니다.', 'ACTIVE', 2),
    ('community2@olma.local',  'QNA',  '프론트 단독 외주 견적 산정 기준이 궁금합니다',        '디자인 시안은 클라이언트가 주고 퍼블+리액트 개발만 하는 건인데, 페이지 수로 잡아야 할지 공수로 잡아야 할지 감이 안 옵니다.', 'ACTIVE', 5),
    ('community3@olma.local',  'QNA',  '웹 UI/UX 리디자인 프로젝트 견적 어떻게 잡으세요?',     '기존 서비스 전면 리디자인인데 화면 수가 유동적입니다. 이런 경우 고정가로 가는지 화면당 단가로 가는지 노하우 부탁드려요.', 'ACTIVE', 7),
    ('community4@olma.local',  'QNA',  '견적서에 상세 산출근거를 요구받는데 어디까지 써야 하나요?', '클라이언트가 항목별 시간과 단가를 다 적어달라고 합니다. 너무 상세히 쓰면 나중에 협상 카드로 쓰이던데 다들 어느 정도로 공개하세요?', 'ACTIVE', 9),
    ('community5@olma.local',  'QNA',  '퍼포먼스 마케팅 대행 수수료 vs 고정비, 뭐가 유리한가요?', '광고비 규모가 들쭉날쭉한 클라이언트라 수수료율로 가면 제 수입도 불안정해집니다. 고정비 전환 경험 있으신 분 조언 부탁해요.', 'ACTIVE', 11),
    ('community6@olma.local',  'QNA',  '영상 편집 건별 단가 책정 팁 있을까요?',               '유튜브 롱폼 편집 건별로 받는데 컷 수랑 자막량이 매번 달라서 단가 정하기가 애매합니다. 기준 잡는 방법 공유해주시면 감사하겠습니다.', 'ACTIVE', 13),
    ('community7@olma.local',  'QNA',  '앱 UI 신입 프리랜서 첫 견적 얼마 부르는 게 맞을까요?',  '이제 막 시작한 앱 UI 디자이너입니다. 지인 소개로 첫 외주가 들어왔는데 너무 싸게 부르기도 비싸게 부르기도 무섭습니다.', 'ACTIVE', 1),
    ('community8@olma.local',  'QNA',  '데이터 파이프라인 구축 외주 기간 산정이 너무 어렵습니다', '소스가 확정되지 않은 상태에서 견적을 달라고 합니다. 요구사항이 계속 바뀔 게 뻔한데 기간과 단가를 어떻게 방어하시나요?', 'ACTIVE', 15),
    ('community9@olma.local',  'QNA',  '기술 문서 번역 장당 vs 단어당 단가, 어떻게 협의하세요?',  '개발 문서 번역인데 코드블록이 많아 장당으로 하면 손해입니다. 단어당으로 유도하는 협의 멘트가 있을까요?', 'ACTIVE', 18),
    ('community10@olma.local', 'QNA',  '기획서만 납품하는 프로젝트 단가는 어떻게 잡나요?',      '개발/디자인 없이 서비스 기획서와 IA만 납품하는 건입니다. 산출물이 문서라 가치를 어떻게 값으로 환산할지 고민입니다.', 'ACTIVE', 20),
    -- INFO
    ('community1@olma.local',  'INFO', '3.3% 원천징수와 종합소득세, 프리랜서라면 알아야 할 정리', '외주 정산 시 떼이는 3.3%가 세금의 끝이 아니라 선납이라는 걸 첫 해에 몰라서 고생했습니다. 5월 종소세 때 어떻게 정산되는지 정리해봤어요.', 'ACTIVE', 22),
    ('community3@olma.local',  'INFO', '외주 계약서에 꼭 넣어야 하는 독소조항 체크리스트',       '수정 횟수 제한, 지연 배상, 저작권 귀속 시점, 하자보수 기간. 이 네 가지만 명시해도 분쟁이 확 줄어듭니다. 실제 문구 예시도 첨부합니다.', 'ACTIVE', 3),
    ('community4@olma.local',  'INFO', '견적서 작성할 때 자주 빠뜨리는 항목 5가지',             '커뮤니케이션 공수, 수정 대응, 배포/이관, 유지보수 유예, 세금계산서 발행분. 이거 빼먹고 견적 내면 결국 무급 노동이 됩니다.', 'ACTIVE', 25),
    ('community5@olma.local',  'INFO', '광고비와 대행비를 분리 청구해야 하는 이유',             '광고비를 대행비에 합쳐 청구했다가 미집행분 환불 요구에 휘말린 적이 있습니다. 계정 소유권과 정산 흐름을 분리하는 게 안전합니다.', 'ACTIVE', 6),
    ('community2@olma.local',  'INFO', '수정 요청 무한루프 막는 계약 문구 공유합니다',           '기본 수정 2회 포함, 초과 시 회당 추가 비용 명시. 이 한 줄로 끝없는 톡을 막았습니다. 실제로 쓰는 문장 그대로 올려요.', 'ACTIVE', 27),
    ('community6@olma.local',  'INFO', '저작권/BGM 라이선스 때문에 문제 생겼던 경험 정리',       '무료 폰트/BGM인 줄 알고 썼다가 상업 이용 불가라 재작업했습니다. 납품 전 라이선스 확인 체크리스트를 공유합니다.', 'ACTIVE', 30),
    ('community8@olma.local',  'INFO', '착수금 비율 얼마로 받는 게 안전한지 정리',               '무착수금은 절대 비추입니다. 최소 30~50% 착수금 + 중도금 구조로 바꾸고 나서 미수금이 사라졌습니다.', 'ACTIVE', 4),
    ('community7@olma.local',  'INFO', '포트폴리오 단가 협상에 실제로 도움 됐던 자료들',         '레퍼런스 단가표, 유사 프로젝트 후기, 공수 산정 근거. 이 세 가지를 미리 준비해두면 협상 테이블에서 밀리지 않습니다.', 'ACTIVE', 32),
    -- FREE
    ('community10@olma.local', 'FREE', '오늘 클라이언트한테 단가 후려치기 당한 썰 풉니다',       '기획서 다 보여주고 나니 예산이 반토막이라네요. 그럴 거면 처음부터 말을 하지... 다들 이런 경험 한 번씩 있으시죠?', 'ACTIVE', 1),
    ('community1@olma.local',  'FREE', '6개월 만에 외주 단가 두 배 올린 후기',                 'OLma 단가 데이터 보고 제가 너무 싸게 받고 있었다는 걸 깨닫고 조금씩 올렸습니다. 결과적으로 일은 줄고 수입은 늘었어요.', 'ACTIVE', 35),
    ('community9@olma.local',  'FREE', '번역 외주 세금계산서 발행 요청 받았는데 멘붕',           '개인 프리랜서인데 세금계산서를 달라네요. 3.3%랑 뭐가 다른 건지 처음이라 하나도 모르겠습니다... 경험담 좀 나눠주세요.', 'ACTIVE', 8),
    ('community2@olma.local',  'FREE', '프리랜서 3년차, 이제야 견적에 자신감이 생겼어요',        '초반엔 견적 부를 때마다 손이 떨렸는데 이제는 근거를 대며 협상합니다. 시작하는 분들 너무 위축되지 마세요.', 'ACTIVE', 12),
    ('community6@olma.local',  'FREE', '명절에도 수정 요청 오는 클라이언트 대처법 토론해요',      '연휴 첫날부터 수정 톡이 왔습니다. 응답 가능 시간을 계약에 못 박아야 하나 고민되네요. 다들 어떻게 선을 그으세요?', 'ACTIVE', 2),
    ('community5@olma.local',  'FREE', 'OLma 단가 데이터 보고 내가 너무 싸게 받았구나 깨달음',    '같은 경력대 중위값 보고 충격받았습니다. 다음 계약부터는 데이터 캡처해서 근거로 들이밀 생각입니다.', 'ACTIVE', 6),
    ('community3@olma.local',  'FREE', '계약서 없이 일했다가 미수금 생긴 이야기 (반성)',         '지인이라 믿고 구두로 진행했다가 결국 절반도 못 받았습니다. 아무리 친해도 계약서는 쓰세요. 정말입니다.', 'HIDDEN', 38)
) AS v(email, category, title, content, status, age_days)
JOIN users u ON u.email = v.email
LEFT JOIN job_categories jc ON jc.id = u.job_category_id
LEFT JOIN experience_levels el ON el.id = u.experience_level_id;

-- ---------------------------------------------------------------------------
-- 3. 최상위 댓글. 작성자 프로필 스냅샷 복사. 게시글은 title로 매칭.
-- ---------------------------------------------------------------------------
INSERT INTO community_comments (
    post_id, user_id, parent_comment_id, content,
    author_job_category_id, author_job_category_name,
    author_experience_level_id, author_experience_level_label,
    created_at
)
SELECT
    p.id, u.id, NULL, v.content,
    u.job_category_id, jc.name,
    u.experience_level_id, el.label,
    now() - make_interval(days => v.age_days)
FROM (VALUES
    ('외주 백엔드 유지보수 월 단가 어느 정도가 적정할까요?', 'community8@olma.local',  '유지보수는 장애 대응 SLA를 얼마나 요구하느냐에 따라 달라집니다. 상시 대기면 개발 단가보다 높게 잡으세요.', 1),
    ('외주 백엔드 유지보수 월 단가 어느 정도가 적정할까요?', 'community2@olma.local',  '월 몇 시간까지 포함인지 상한을 꼭 정하세요. 무제한처럼 되면 답 없습니다.', 1),
    ('프론트 단독 외주 견적 산정 기준이 궁금합니다',        'community3@olma.local',  '페이지 수보다 컴포넌트/상태 복잡도로 잡는 게 정확합니다. 리스트+상세만 있어도 필터 붙으면 공수 배로 뜁니다.', 3),
    ('웹 UI/UX 리디자인 프로젝트 견적 어떻게 잡으세요?',     'community7@olma.local',  '화면 수 유동적이면 1차 화면 확정까지를 별도 단계로 끊어서 견적 내는 걸 추천해요.', 4),
    ('견적서에 상세 산출근거를 요구받는데 어디까지 써야 하나요?', 'community1@olma.local', '항목은 공개하되 항목별 단가까지는 총액 기준으로만 보여주는 편입니다. 라인아이템 단가는 협상 빌미가 돼요.', 5),
    ('퍼포먼스 마케팅 대행 수수료 vs 고정비, 뭐가 유리한가요?', 'community4@olma.local', '광고비 변동이 크면 고정비 + 성과 인센티브 하이브리드가 서로 마음 편합니다.', 6),
    ('영상 편집 건별 단가 책정 팁 있을까요?',               'community9@olma.local',  '기본 컷 수/자막 분량 구간을 정하고 초과분은 추가 과금으로 명시하면 매번 협상 안 해도 됩니다.', 5),
    ('앱 UI 신입 프리랜서 첫 견적 얼마 부르는 게 맞을까요?',  'community3@olma.local',  '신입이라고 후려치지 마세요. OLma에서 같은 경력대 중위값 보고 그 근처로 부르면 됩니다.', 1),
    ('앱 UI 신입 프리랜서 첫 견적 얼마 부르는 게 맞을까요?',  'community4@olma.local',  '지인 건일수록 계약서 꼭 쓰세요. 싸게 해주고 관계까지 상하는 게 최악입니다.', 1),
    ('데이터 파이프라인 구축 외주 기간 산정이 너무 어렵습니다', 'community1@olma.local', '요구사항 미확정이면 정액 대신 인력 투입 단위(맨먼스) 계약으로 방어하는 게 정석입니다.', 4),
    ('기술 문서 번역 장당 vs 단어당 단가, 어떻게 협의하세요?', 'community10@olma.local', '코드블록/표는 제외 단어수로 산정한다고 견적서에 명시해두면 깔끔합니다.', 3),
    ('3.3% 원천징수와 종합소득세, 프리랜서라면 알아야 할 정리', 'community9@olma.local', '이거 첫 해에 몰라서 5월에 토해냈습니다. 경비 증빙 잘 모아두는 게 진짜 중요해요.', 5),
    ('외주 계약서에 꼭 넣어야 하는 독소조항 체크리스트',       'community2@olma.local',  '저작권 귀속 시점 = 잔금 완납 시점으로 걸어두는 거 정말 중요합니다. 미수금 방어됩니다.', 1),
    ('견적서 작성할 때 자주 빠뜨리는 항목 5가지',             'community5@olma.local',  '커뮤니케이션 공수 진짜 공감합니다. 회의만 잡아도 반나절 날아가는데 무료로 취급되더라고요.', 2),
    ('수정 요청 무한루프 막는 계약 문구 공유합니다',           'community6@olma.local',  '문구 그대로 가져다 쓰겠습니다. 감사합니다. 저는 여기에 응답 가능 시간대까지 추가했어요.', 3),
    ('착수금 비율 얼마로 받는 게 안전한지 정리',               'community2@olma.local',  '무착수금 진짜 절대 비추 동의합니다. 착수금 안 주는 곳은 잔금도 잘 안 줍니다.', 1),
    ('오늘 클라이언트한테 단가 후려치기 당한 썰 풉니다',       'community1@olma.local',  '고생하셨습니다... 예산 먼저 확인하고 상세 산출물 공개하는 순서로 바꾸시면 좀 나아요.', 1),
    ('6개월 만에 외주 단가 두 배 올린 후기',                 'community5@olma.local',  '멋지네요. 저도 데이터 근거로 올려보려는 참인데 용기 얻고 갑니다.', 2),
    ('명절에도 수정 요청 오는 클라이언트 대처법 토론해요',      'community4@olma.local',  '자동응답으로 업무일 기준 응답 시간을 안내하는 것만으로도 기대치가 확 조정됩니다.', 1),
    ('번역 외주 세금계산서 발행 요청 받았는데 멘붕',           'community8@olma.local',  '개인이라도 홈택스에서 발급 가능합니다. 3.3%랑은 별개 흐름이니 세무 상담 한 번 받아보세요.', 3)
) AS v(post_title, email, content, age_days)
JOIN community_posts p ON p.title = v.post_title
JOIN users u ON u.email = v.email
LEFT JOIN job_categories jc ON jc.id = u.job_category_id
LEFT JOIN experience_levels el ON el.id = u.experience_level_id;

-- ---------------------------------------------------------------------------
-- 4. 대댓글. 부모 댓글은 (게시글 title + 부모 댓글 내용)으로 매칭.
-- ---------------------------------------------------------------------------
INSERT INTO community_comments (
    post_id, user_id, parent_comment_id, content,
    author_job_category_id, author_job_category_name,
    author_experience_level_id, author_experience_level_label,
    created_at
)
SELECT
    parent.post_id, u.id, parent.id, v.content,
    u.job_category_id, jc.name,
    u.experience_level_id, el.label,
    now() - make_interval(days => v.age_days)
FROM (VALUES
    ('외주 백엔드 유지보수 월 단가 어느 정도가 적정할까요?', '유지보수는 장애 대응 SLA를 얼마나 요구하느냐에 따라 달라집니다. 상시 대기면 개발 단가보다 높게 잡으세요.', 'community1@olma.local', '오 SLA 관점 생각 못 했네요. 상시 대기 조건 빼는 방향으로 협의해봐야겠습니다.', 0),
    ('앱 UI 신입 프리랜서 첫 견적 얼마 부르는 게 맞을까요?', '신입이라고 후려치지 마세요. OLma에서 같은 경력대 중위값 보고 그 근처로 부르면 됩니다.', 'community7@olma.local', '감사합니다! 중위값 캡처해서 근거로 제시해봤더니 바로 오케이 받았어요.', 0),
    ('외주 계약서에 꼭 넣어야 하는 독소조항 체크리스트', '저작권 귀속 시점 = 잔금 완납 시점으로 걸어두는 거 정말 중요합니다. 미수금 방어됩니다.', 'community3@olma.local', '맞아요, 이 조항 하나로 잔금 회수율이 확 올라갔습니다.', 1),
    ('번역 외주 세금계산서 발행 요청 받았는데 멘붕', '개인이라도 홈택스에서 발급 가능합니다. 3.3%랑은 별개 흐름이니 세무 상담 한 번 받아보세요.', 'community9@olma.local', '홈택스에서 되는군요! 한시름 놓았습니다. 감사합니다.', 0)
) AS v(post_title, parent_content, email, content, age_days)
JOIN community_posts p ON p.title = v.post_title
JOIN community_comments parent ON parent.post_id = p.id AND parent.content = v.parent_content
JOIN users u ON u.email = v.email
LEFT JOIN job_categories jc ON jc.id = u.job_category_id
LEFT JOIN experience_levels el ON el.id = u.experience_level_id;

-- ---------------------------------------------------------------------------
-- 5. 게시글 좋아요. 커뮤니티 유저 × 게시글 조합에서 결정적 분포로 생성.
--    본인 글 제외, HIDDEN 글 제외. UNIQUE(post_id,user_id) 자동 충족.
-- ---------------------------------------------------------------------------
INSERT INTO community_post_likes (post_id, user_id, created_at)
SELECT p.id, u.id, now() - make_interval(hours => ((p.id * 7 + u.id * 3) % 240)::int)
FROM community_posts p
JOIN users u ON u.email LIKE 'community%@olma.local'
WHERE p.status = 'ACTIVE'
  AND u.id <> p.user_id
  AND ((p.id * 3 + u.id * 5) % 7) < 4;

-- ---------------------------------------------------------------------------
-- 6. 댓글 좋아요. 동일한 결정적 분포.
-- ---------------------------------------------------------------------------
INSERT INTO community_comment_likes (comment_id, user_id, created_at)
SELECT c.id, u.id, now() - make_interval(hours => ((c.id * 5 + u.id * 2) % 200)::int)
FROM community_comments c
JOIN users u ON u.email LIKE 'community%@olma.local'
WHERE c.status = 'ACTIVE'
  AND u.id <> c.user_id
  AND ((c.id * 4 + u.id * 3) % 9) < 3;

-- ---------------------------------------------------------------------------
-- 7. 게시글 이미지 (일부 글). sort_order 0~2, UNIQUE(post_id,sort_order).
-- ---------------------------------------------------------------------------
INSERT INTO community_post_images (post_id, image_url, sort_order)
SELECT p.id, v.image_url, v.sort_order
FROM (VALUES
    ('웹 UI/UX 리디자인 프로젝트 견적 어떻게 잡으세요?', 'https://picsum.photos/seed/olma-redesign-1/1000/700', 0),
    ('웹 UI/UX 리디자인 프로젝트 견적 어떻게 잡으세요?', 'https://picsum.photos/seed/olma-redesign-2/1000/700', 1),
    ('저작권/BGM 라이선스 때문에 문제 생겼던 경험 정리',   'https://picsum.photos/seed/olma-license-1/1000/700', 0),
    ('포트폴리오 단가 협상에 실제로 도움 됐던 자료들',     'https://picsum.photos/seed/olma-portfolio-1/1000/700', 0),
    ('포트폴리오 단가 협상에 실제로 도움 됐던 자료들',     'https://picsum.photos/seed/olma-portfolio-2/1000/700', 1),
    ('포트폴리오 단가 협상에 실제로 도움 됐던 자료들',     'https://picsum.photos/seed/olma-portfolio-3/1000/700', 2)
) AS v(post_title, image_url, sort_order)
JOIN community_posts p ON p.title = v.post_title;

-- ---------------------------------------------------------------------------
-- 8. 신고. 신고자당 (post) / (comment) UNIQUE. HIDDEN 처리된 글에 신고 누적.
-- ---------------------------------------------------------------------------
INSERT INTO community_reports (reporter_id, post_id, comment_id, reason, detail)
SELECT u.id, p.id, NULL, v.reason, v.detail
FROM (VALUES
    ('계약서 없이 일했다가 미수금 생긴 이야기 (반성)', 'community5@olma.local',  'SPAM',       '동일 내용 반복 게시로 보입니다.'),
    ('계약서 없이 일했다가 미수금 생긴 이야기 (반성)', 'community8@olma.local',  'ETC',        '특정 업체 비방으로 읽힐 소지가 있어요.'),
    ('계약서 없이 일했다가 미수금 생긴 이야기 (반성)', 'community2@olma.local',  'FALSE_INFO', '사실관계 확인이 필요해 보입니다.'),
    ('오늘 클라이언트한테 단가 후려치기 당한 썰 풉니다', 'community6@olma.local',  'ABUSE',      '표현 수위가 다소 과합니다.')
) AS v(post_title, reporter_email, reason, detail)
JOIN community_posts p ON p.title = v.post_title
JOIN users u ON u.email = v.reporter_email;

-- ---------------------------------------------------------------------------
-- 9. 집계 컬럼을 실제 행 수 기준으로 재계산 (정합성 보장).
-- ---------------------------------------------------------------------------
UPDATE community_posts p SET
    like_count    = COALESCE((SELECT count(*) FROM community_post_likes l WHERE l.post_id = p.id), 0),
    comment_count = COALESCE((SELECT count(*) FROM community_comments c WHERE c.post_id = p.id AND c.status = 'ACTIVE'), 0),
    report_count  = COALESCE((SELECT count(*) FROM community_reports r WHERE r.post_id = p.id), 0)
WHERE p.user_id IN (SELECT id FROM users WHERE email LIKE 'community%@olma.local');

UPDATE community_comments c SET
    like_count   = COALESCE((SELECT count(*) FROM community_comment_likes cl WHERE cl.comment_id = c.id), 0),
    report_count = COALESCE((SELECT count(*) FROM community_reports r WHERE r.comment_id = c.id), 0)
WHERE c.user_id IN (SELECT id FROM users WHERE email LIKE 'community%@olma.local');
