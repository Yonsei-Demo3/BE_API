package com.backend.question;


import com.backend.content.domain.Content;
import com.backend.content.repository.ContentRepository;
import com.backend.member.domain.Member;
import com.backend.member.repository.MemberRepository;
import com.backend.question.domain.Question;
import com.backend.question.dto.request.CreateFirstQuestionRequestDTO;
import com.backend.question.dto.response.CreateQuestionResponseDTO;
import com.backend.question.repository.QuestionRepository;
import com.backend.room.domain.Room;
import com.backend.room.repository.RoomRepository;

public class QuestionService {

    private QuestionRepository questionRepository;
    private ContentRepository contentRepository;
    private RoomRepository roomRepository;
    private MemberRepository memberRepository;

    //TODO: Security 반영
    public CreateQuestionResponseDTO createFirstQuestion(Long hostId, CreateFirstQuestionRequestDTO dto) {
        Member host = memberRepository.findById(hostId)
                .orElseThrow(() -> new RuntimeException("member not found"));


        Content content = contentRepository.findById(dto.contentId())
                .orElseThrow(() -> new RuntimeException("Content not found"));

        //TODO: Room이 id만 가지지에는 쫌 그렇긴하네... 그래도 아마 추후 메시지 추가 예정
        Room newRoom = new Room();
        roomRepository.save(newRoom);

        Question question = Question.createFirstQuestionOf(
                dto.title(),
                dto.description(),
                dto.maxParticipants(),
                host,
                newRoom,
                content
        );
        questionRepository.save(question);

        return CreateQuestionResponseDTO.from(question);
    }


}
