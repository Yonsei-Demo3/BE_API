package com.backend.question;

import com.backend.content.domain.Content;
import com.backend.content.repository.ContentRepository;
import com.backend.member.domain.Member;
import com.backend.member.repository.MemberRepository;
import com.backend.question.domain.Question;
import com.backend.question.dto.request.CreateFirstQuestionRequestDTO;
import com.backend.question.dto.response.CreateQuestionResponseDTO;
import com.backend.question.dto.response.QuestionResponseDTO;
import com.backend.question.repository.QuestionRepository;
import com.backend.room.domain.Room;
import com.backend.room.repository.RoomRepository;
import com.backend.roomMember.domain.RoomMember;
import com.backend.roomMember.repository.RoomMemberRepository;
import com.backend.tag.Tag;
import com.backend.tag.TagService;
import com.backend.tagQuestion.TagQuestion;
import com.backend.tagQuestion.TagQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public CreateQuestionResponseDTO createFirstQuestion(String hostUserId, CreateFirstQuestionRequestDTO dto) {
        Member host = memberRepository.findByUserId(hostUserId)
                .orElseThrow(() -> new RuntimeException("member not found"));

        Content content = contentRepository.findById(dto.contentId())
                .orElseThrow(() -> new RuntimeException("Content not found"));

        //TODO: 태그 추가

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

        List<Tag> tagEntities = tagService.findOrCreateTags(dto.tags());

        for (Tag tag : tagEntities) {
            TagQuestion newTagQuestion = TagQuestion.of(tag,savedQuestion);
            tagQuestionRepository.save(newTagQuestion);
        }
        //TODO: DTO에 태그 반영
        return CreateQuestionResponseDTO.from(question);
    }

    //질문 참여
    @Transactional
    public QuestionResponseDTO participateQuestion(String hostUserId, Long questionId) {
        Member participants = memberRepository.findByUserId(hostUserId)
                .orElseThrow(() -> new RuntimeException("member not found"));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("question not found"));

        Room room = question.getRoom();

        if(roomMemberRepository.existsByRoomAndMember(room, participants)) {
            throw new RuntimeException("이미 참가한 질문 채팅방입니다.");
        }

        RoomMember newMember = RoomMember.of(room, participants);
        roomMemberRepository.save(newMember);

        return QuestionResponseDTO.from(question);
    }
}
