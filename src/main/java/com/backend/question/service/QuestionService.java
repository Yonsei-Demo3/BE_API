package com.backend.question.service;

import com.backend.category.domain.Category;
import com.backend.categoryContent.CategoryContent;
import com.backend.categoryContent.CategoryContentRepository;
import com.backend.content.domain.Content;
import com.backend.content.repository.ContentRepository;
import com.backend.member.domain.Member;
import com.backend.member.repository.MemberRepository;
import com.backend.notification.Notification;
import com.backend.notification.NotificationMessage;
import com.backend.notification.NotificationPublisher;
import com.backend.notification.NotificationRepository;
import com.backend.notification.NotificationType;
import com.backend.question.domain.ParticipationStatus;
import com.backend.question.domain.Question;
import com.backend.question.domain.QuestionStatus;
import com.backend.question.dto.request.CreateFirstQuestionRequestDTO;
import com.backend.question.dto.request.QuestionSearchRequestDTO;
import com.backend.question.dto.response.CreateQuestionResponseDTO;
import com.backend.question.dto.response.QuestionDTO;
import com.backend.question.dto.response.QuestionDetailResponseDTO;
import com.backend.question.dto.response.QuestionMembersResponseDTO;
import com.backend.question.dto.response.QuestionResponseDTO;
import com.backend.question.repository.QuestionRepository;
import com.backend.like.repository.QuestionLikeRepository;
import com.backend.room.domain.Room;
import com.backend.room.repository.RoomRepository;
import com.backend.roomMember.domain.RoomMember;
import com.backend.roomMember.repository.RoomMemberRepository;
import com.backend.tag.Tag;
import com.backend.tag.TagService;
import com.backend.tagQuestion.TagQuestion;
import com.backend.tagQuestion.TagQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ContentRepository contentRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final TagService tagService;
    private final TagQuestionRepository tagQuestionRepository;
    private final QuestionDtoAssembler questionDtoAssembler;
    private final NotificationPublisher notificationPublisher;
    private final NotificationRepository notificationRepository;
    private final CategoryContentRepository categoryContentRepository;
    private final QuestionLikeRepository likeRepository;

    @Transactional
    public CreateQuestionResponseDTO createFirstQuestion(String hostUserId, CreateFirstQuestionRequestDTO dto) {
        Member host = memberRepository.findByUserId(hostUserId)
                .orElseThrow(() -> new RuntimeException("member not found"));

        Content content = contentRepository.findById(dto.contentId())
                .orElseThrow(() -> new RuntimeException("Content not found"));

        //TODO: Room이 id만 가지지에는 쫌 그렇긴하네... 그래도 아마 추후 메시지 추가 예정
        Room newRoom = new Room();
        roomRepository.save(newRoom);

        //RoomMember 테이블에 호스트 추가
        List<String> usedNicknames = roomMemberRepository.findNicknamesByRoomId(newRoom.getId());
        RoomMember newMember = RoomMember.of(
                newRoom,
                host,
                new HashSet<>()
        );
        roomMemberRepository.save(newMember);

        Question question = Question.createFirstQuestionOf(
                dto.title(),
                dto.description(),
                dto.maxParticipants(),
                dto.startMode(),
                host,
                newRoom,
                content
        );
        Question savedQuestion = questionRepository.save(question);

        List<Tag> tagEntities = tagService.findOrCreateTags(dto.tags() != null ? dto.tags() : java.util.Collections.emptyList());

        for (Tag tag : tagEntities) {
            TagQuestion newTagQuestion = TagQuestion.of(tag, savedQuestion);
            tagQuestionRepository.save(newTagQuestion);
        }
        //TODO: DTO에 태그 반영
        return CreateQuestionResponseDTO.from(question);
    }


    //질문 참여
    //TODO: 코드가 쫌 복잡해지는 거 같기도... 특히 for문을 분리하는 방법 고려해야 할 듯
    @Transactional
    public QuestionDTO participateQuestion(String hostUserId, Long questionId) {
        Member participant = memberRepository.findByUserId(hostUserId)
                .orElseThrow(() -> new RuntimeException("member not found"));

        Question question = questionRepository.findByIdWithLock(questionId)
                .orElseThrow(() -> new RuntimeException("question not found"));

        Room room = question.getRoom();

        List<String> usedNicknames = 
        roomMemberRepository.findNicknamesByRoomId(room.getId());

        if (question.getCurrentParticipants() >= question.getMaxParticipants()) {
            throw new RuntimeException("참여 인원이 초과되었습니다.");
        }

        if(roomMemberRepository.existsByRoomAndMember(room, participant)) {
            throw new RuntimeException("이미 참가한 질문 채팅방입니다.");
        }

        question.increaseCurrentParticipants();//DirtyChecking으로 저장

        RoomMember newMember = RoomMember.of(
                room,
                participant,
                new HashSet<>(usedNicknames)
        );
        roomMemberRepository.save(newMember);

        //TODO:로직 간소화....
        if (question.getCurrentParticipants()==(question.getMaxParticipants())) {

            List<RoomMember> roomMembers = roomMemberRepository.findAllByRoom(room);

            List<Notification> notifications = new ArrayList<>();

            Map<String, Object> notifyData = new java.util.HashMap<>();
            notifyData.put("roomId", room.getId());
            notifyData.put("questionId", question.getId());

            String alertMessage = "질문 방 인원이 마감되었습니다! 준비해주세요.";

            for (RoomMember rm : roomMembers) {
                Member receiver = rm.getMember();

                notifications.add(Notification.of(receiver, NotificationType.QUESTION_FULL));

                //TODO: 데이터 베이스 저장 후 알림 전송할 수 있도록 EventListener? 추가
                try {

                    NotificationMessage message = NotificationMessage.of(receiver.getId(), NotificationType.QUESTION_FULL, alertMessage, notifyData);
                    notificationPublisher.publish(message);
                    //시간을 저장....
                } catch (Exception e) {
                    // 에러가 나도 throw 하지 않고 로그만 찍음 (트랜잭션 유지)
                    System.err.println("redis 알림 발송 실패 (사용자는 정상 참여됨): " + e.getMessage());
                    // 실제 운영에선 log.error("...", e); 사용
                }
            }
            notificationRepository.saveAll(notifications);
            question.recruitingToReadyCheck();
        }

        return QuestionDTO.from(question);
    }

    public List<QuestionResponseDTO> getMyWrittenQuestions(String userId, String order) {
        Member member =  memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("member not found"));

        List<Question> questions = questionRepository.findByHost(member);

        List<Question> ordered = switch (order) {
            case "popular" -> questions.stream()
                    .sorted(Comparator
                            .comparingInt(Question::getCurrentParticipants)
                            .reversed())
                    .toList();
            case "oldest" -> questions.stream()
                    .sorted(Comparator.comparing(Question::getCreatedAt))
                    .toList();
            case "latest" -> questions.stream()
                    .sorted(Comparator.comparing(Question::getCreatedAt).reversed())
                    .toList();
            default -> questions;  // 이상한 값이면 그냥 원래 순서
        };

        Map<Long, ParticipationStatus> myStatusMap = questions.stream()
        .collect(Collectors.toMap(
                Question::getId,
                q -> ParticipationStatus.JOINED
        ));

        Map<Long, Integer> likeCountMap = questions.stream()
        .collect(Collectors.toMap(
                Question::getId,
                Question::getLikeCount
        ));

        List<Long> questionIds = ordered.stream().map(Question::getId).toList();
        List<Long> likedIds = questionIds.isEmpty()
                ? Collections.emptyList()
                : likeRepository.findLikedQuestionIds(userId, questionIds);

        Map<Long, Boolean> isLikedByMeMap = likedIds.stream()
                .collect(Collectors.toMap(id -> id, id -> true));

        return questionDtoAssembler.toListDto(
                ordered,
                myStatusMap,
                likeCountMap,
                isLikedByMeMap
        );
    }

    public List<QuestionResponseDTO> getQuestions(String userId, String sort, String order) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("member not found"));

        List<RoomMember> myRoomMembers = roomMemberRepository.findAllByMember(member);

        // 3. RoomMember에서 Room 정보만 쏙 추출
        List<Room> myRooms = myRoomMembers.stream()
                .map(RoomMember::getRoom)
                .toList();

        if (myRooms.isEmpty()) {
            return List.of();
        }

        QuestionStatus targetStatus = null;

        if (sort != null) {
            switch (sort) {
                case "participate" -> targetStatus = QuestionStatus.RECRUITING;  // 모집 중
                case "ready" -> targetStatus = QuestionStatus.READY_CHECK; // 준비 중
                case "finish" -> targetStatus = QuestionStatus.FINISHED;    // 종료됨 (또는 ACTIVE 포함 가능)
                // default -> null (전체 조회)
            }
        }

        List<Question> questions;

        if (targetStatus != null) {
            questions = questionRepository.findByRoomInAndStatus(myRooms, targetStatus);
        } else {
            questions = questionRepository.findByRoomIn(myRooms);
        }

        String effectiveOrder = order;
        
        List<Question> ordered = switch (effectiveOrder) {
            case "popular" -> questions.stream()
                    .sorted(Comparator.comparingInt(Question::getCurrentParticipants).reversed())
                    .toList();
            case "oldest" -> questions.stream()
                    .sorted(Comparator.comparing(Question::getCreatedAt))
                    .toList();
            case "latest" -> questions.stream()
                    .sorted(Comparator.comparing(Question::getCreatedAt).reversed())
                    .toList();
            default -> questions; // 이상한 값 들어오면 그냥 기존 순서 유지
        };

        Map<Long, ParticipationStatus> myStatusMap = questions.stream()
                .collect(Collectors.toMap(
                        Question::getId,
                        q -> {
                            if (q.getStatus() == QuestionStatus.ACTIVE) {
                                return ParticipationStatus.JOINED;
                            } else {
                                return ParticipationStatus.WAITING;
                            }
                        }
                ));

        Map<Long, Integer> likeCountMap = questions.stream()
        .collect(Collectors.toMap(
                Question::getId,
                Question::getLikeCount
        ));
    
        List<Long> questionIds = ordered.stream().map(Question::getId).toList();
        List<Long> likedIds = questionIds.isEmpty()
                ? Collections.emptyList()
                : likeRepository.findLikedQuestionIds(userId, questionIds);
    
        Map<Long, Boolean> isLikedByMeMap = likedIds.stream()
                .collect(Collectors.toMap(id -> id, id -> true));
    
        return questionDtoAssembler.toListDto(
                ordered,
                myStatusMap,
                likeCountMap,
                isLikedByMeMap
        );
    }
    //TODO: N+1 문제 해결
    public QuestionDetailResponseDTO getQuestionDetailById(Long questionId, String userId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("question not found"));
    
        List<Tag> tags = tagQuestionRepository.findAllByQuestion(question)
                .stream()
                .map(TagQuestion::getTag)
                .toList();
    
        CategoryContent categoryContent  = categoryContentRepository.findByContent(question.getContent())
                .orElseThrow(() -> new RuntimeException("category 찾을 수 없음"));
    
        Category subCategory = categoryContent.getCategory();
        Category mainCategory = subCategory.getParent();
    
        ParticipationStatus myStatus = ParticipationStatus.NONE;
    
        if (userId != null) {
            Member me = memberRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("member not found"));
    
            Room room = question.getRoom();
    
            boolean isRoomMember = roomMemberRepository.existsByRoomAndMember(room, me);
    
            if (isRoomMember) {
                // 🔹 ACTIVE면 JOINED, 그 외(RECRUITING/READY_CHECK)는 WAITING
                if (question.getStatus() == QuestionStatus.ACTIVE) {
                    myStatus = ParticipationStatus.JOINED;
                } else if (question.getStatus() == QuestionStatus.RECRUITING ||
                           question.getStatus() == QuestionStatus.READY_CHECK) {
                    myStatus = ParticipationStatus.WAITING;
                }
            }
        }
    
        return QuestionDetailResponseDTO.from(
                question,
                tags,
                mainCategory,
                subCategory,
                myStatus
        );
    }

    //TODO: 진행중이거나 끝났을 땐 못하게 막아야함
    @Transactional
    public void cancelParticipateById(Long questionId, String userId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("member not found"));

        Question question = questionRepository.findByIdWithLock(questionId)
                .orElseThrow(() -> new RuntimeException("question not found"));

        Room room = question.getRoom();

        RoomMember roomMember = roomMemberRepository.findByRoomAndMember(room, member)
                .orElseThrow(() -> new RuntimeException("room member not found"));

        if (question.getHost().getId().equals(member.getId())) {
            throw new RuntimeException("방장은 참여를 취소할 수 없습니다. 방을 삭제해주세요.");
        }

        roomMemberRepository.delete(roomMember);

        question.decreaseCurrentParticipants();
    }

    @Transactional
    public QuestionDTO readyQuestionById(Long questionId, String userId) {

        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("member not found"));

        Question question = questionRepository.findByIdWithLock(questionId)
                .orElseThrow(() -> new RuntimeException("question not found"));

        Room room = question.getRoom();

        RoomMember roomMember = roomMemberRepository.findByRoomAndMember(room, member)
                .orElseThrow(() -> new RuntimeException("참여하지 않은 방입니다."));

        roomMember.doReady();

        if (question.getHost().getId().equals(member.getId())) {
            if (question.getStatus() != QuestionStatus.READY_CHECK) {
                throw new RuntimeException("질문 상태가 READY_CHECK가 아닙니다. 상태 전환이 불가능합니다.");
            }
            question.readyCheckToActive();
            //42분 뒤 종료되게 스케줄러 등록!
        }

        return QuestionDTO.from(question);
    }

    @Transactional
    public QuestionDTO finishQuestionById(Long questionId, String userId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("member not found"));

        Question question = questionRepository.findByIdWithLock(questionId)
                .orElseThrow(() -> new RuntimeException("question not found"));

        //TODO: 방장 로직 검증 추가

        if (question.getStatus() != QuestionStatus.ACTIVE) {
            throw new RuntimeException("질문 상태가 ACTIVE가 아닙니다.");
        }
        question.activeToFinished();

        return QuestionDTO.from(question);
    }

    public QuestionDTO getTimeById(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("question not found"));
        return QuestionDTO.from(question);
    }

    public QuestionMembersResponseDTO getQuestionMembers(Long questionId, String userId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("member not found"));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("question not found"));
        Room room = question.getRoom();

        List<RoomMember> roomMembers = roomMemberRepository.findAllByRoom(room);

        List<Member> members = roomMembers.stream()
                .map(RoomMember::getMember)
                .toList();

        return QuestionMembersResponseDTO.of(members, member.getId());
    }

    public Page<QuestionResponseDTO> searchQuestions(String userId, QuestionSearchRequestDTO dto, Pageable pageable) {
        Page<Question> questionPage = questionRepository.search(dto, pageable);
        List<Question> questions = questionPage.getContent();
        Map<Long, ParticipationStatus> myStatusMap = java.util.Collections.emptyMap();

        if (userId != null) {
            Member me = memberRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("member not found"));
    
            // 내가 속한 방들
            List<RoomMember> myRoomMembers = roomMemberRepository.findAllByMember(me);
            java.util.Set<Long> myRoomIds = myRoomMembers.stream()
                    .map(rm -> rm.getRoom().getId())
                    .collect(java.util.stream.Collectors.toSet());
    
            // 이번 검색 결과 중, 내가 속한 방의 질문만 WAITING/JOINED로 표시
            myStatusMap = questionPage.getContent().stream()
                    .filter(q -> myRoomIds.contains(q.getRoom().getId()))
                    .collect(Collectors.toMap(
                            Question::getId,
                            q -> (q.getStatus() == QuestionStatus.ACTIVE)
                                    ? ParticipationStatus.JOINED
                                    : ParticipationStatus.WAITING
                    ));
        }

        Map<Long, Integer> likeCountMap = questions.stream()
        .collect(Collectors.toMap(
                Question::getId,
                Question::getLikeCount
        ));

        Map<Long, Boolean> isLikedByMeMap = Collections.emptyMap();

        if (userId != null) {
            List<Long> ids = questions.stream().map(Question::getId).toList();
    
            List<Long> likedIds = ids.isEmpty()
                ? Collections.emptyList()
                : likeRepository.findLikedQuestionIds(userId, ids);
    
            isLikedByMeMap = likedIds.stream()
                    .collect(Collectors.toMap(id -> id, id -> true));
        }

        return questionDtoAssembler.toPageDto(
            questionPage,
            myStatusMap,
            likeCountMap,
            isLikedByMeMap
    );
    }
}
