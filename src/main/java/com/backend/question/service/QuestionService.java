package com.backend.question.service;

import com.backend.category.domain.Category;
import com.backend.content.domain.Content;
import com.backend.content.repository.ContentRepository;
import com.backend.member.domain.Member;
import com.backend.member.repository.MemberRepository;
import com.backend.notification.Notification;
import com.backend.notification.NotificationMessage;
import com.backend.notification.NotificationPublisher;
import com.backend.notification.NotificationRepository;
import com.backend.notification.NotificationType;
import com.backend.question.domain.Question;
import com.backend.question.domain.QuestionStatus;
import com.backend.question.dto.request.CreateFirstQuestionRequestDTO;
import com.backend.question.dto.request.QuestionSearchRequestDTO;
import com.backend.question.dto.response.CreateQuestionResponseDTO;
import com.backend.question.dto.response.QuestionDTO;
import com.backend.question.dto.response.QuestionDetailResponseDTO;
import com.backend.question.dto.response.QuestionResponseDTO;
import com.backend.question.repository.QuestionRepository;
import com.backend.room.domain.Room;
import com.backend.room.repository.RoomRepository;
import com.backend.roomMember.domain.RoomMember;
import com.backend.roomMember.repository.RoomMemberRepository;
import com.backend.tag.Tag;
import com.backend.tag.TagRepository;
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
        RoomMember newMember = RoomMember.of(newRoom, host);
        roomMemberRepository.save(newMember);

        Question question = Question.createFirstQuestionOf(
                dto.title(),
                dto.description(),
                dto.maxParticipants(),
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

        if (question.getCurrentParticipants() >= question.getMaxParticipants()) {
            throw new RuntimeException("참여 인원이 초과되었습니다.");
        }

        if(roomMemberRepository.existsByRoomAndMember(room, participant)) {
            throw new RuntimeException("이미 참가한 질문 채팅방입니다.");
        }

        question.increaseCurrentParticipants();//DirtyChecking으로 저장

        RoomMember newMember = RoomMember.of(room, participant);
        roomMemberRepository.save(newMember);

        //TODO:로직 간소화....
        if (question.getCurrentParticipants()==(question.getMaxParticipants())) {

            List<RoomMember> roomMembers = roomMemberRepository.findAllByRoom(room);

            List<Notification> notifications = new ArrayList<>();

            for (RoomMember rm : roomMembers) {
                Member receiver = rm.getMember();

                notifications.add(Notification.of(receiver, NotificationType.QUESTION_FULL));

                //TODO: 데이터 베이스 저장 후 알림 전송할 수 있도록 EventListener? 추가
                try {
                    NotificationMessage message = NotificationMessage.of(receiver.getId(), NotificationType.QUESTION_FULL);
                    notificationPublisher.publish(message);
                    //시간을 저장....
                } catch (Exception e) {
                    // 에러가 나도 throw 하지 않고 로그만 찍음 (트랜잭션 유지)
                    System.err.println("redis 알림 발송 실패 (사용자는 정상 참여됨): " + e.getMessage());
                    // 실제 운영에선 log.error("...", e); 사용
                }
            }
            notificationRepository.saveAll(notifications);
        }
        return QuestionDTO.from(question);
    }

    //TODO: questionDTOAssembler 사용
    public List<QuestionResponseDTO> getQuestions(String userId, String sort) {
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
        return questions.stream()
                .map(question -> {
                    List<Tag> tags = tagQuestionRepository.findAllByQuestion(question)
                            .stream()
                            .map(TagQuestion::getTag)
                            .toList();

                    //TODO: DTO 수정
                    Category subCategory = null;

                    // (3) DTO 생성 (수정된 DTO 시그니처에 맞춤)
                    return QuestionResponseDTO.from(question, subCategory, tags);
                })
                .toList();
    }

    //TODO: 질문 상세 조회
    public QuestionDetailResponseDTO getQuestionDetailById(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("question not found"));

        List<Tag> tags = tagQuestionRepository.findAllByQuestion(question)
                .stream()
                .map(TagQuestion::getTag)
                .toList();

        return QuestionDetailResponseDTO.from(question, tags);
    }

    @Transactional
    public void deleteQuestionById(Long questionId, String userId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("member not found"));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("question not found"));

        Room room = question.getRoom();



    }

    @Transactional
    public QuestionDTO readyQuestionById(Long questionId, String userId) {

        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("member not found"));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("question not found"));

        Room room = question.getRoom();

        RoomMember roomMember = roomMemberRepository.findByRoomAndMember(room, member)
                .orElseThrow(() -> new RuntimeException("참여하지 않은 방입니다."));

        roomMember.doReady();

        return QuestionDTO.from(question);
    }

    //TODO: 검색
    public Page<QuestionResponseDTO> searchQuestions(QuestionSearchRequestDTO dto, Pageable pageable) {
        Page<Question> questionPage = questionRepository.search(dto, pageable);
        return questionDtoAssembler.toPageDto(questionPage);
    }
}
