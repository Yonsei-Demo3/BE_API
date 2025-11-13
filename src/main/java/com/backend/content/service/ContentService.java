package com.backend.content.service;

import com.backend.content.domain.Content;
import com.backend.content.dto.ContentCreateRequestDTO;
import com.backend.content.dto.ContentResponseDTO;
import com.backend.content.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentService {
    private final ContentRepository contentRepository;

    @Transactional
    public ContentResponseDTO createContent(ContentCreateRequestDTO dto) {
        Content content = Content.of(dto.name(), dto.creator(), dto.description(), dto.imageUrl(), dto.link());
        Content savedContent= contentRepository.save(content);
        return ContentResponseDTO.from(savedContent);
    }
}
