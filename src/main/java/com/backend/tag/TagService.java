package com.backend.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {
    private final TagRepository tagRepository;

    @Transactional
    public List<Tag> findOrCreateTags(List<String> tagNames) {
        return tagNames.stream()
                .map(name -> tagRepository.findByName(name)
                        .orElseGet(()-> {
                            Tag newTag = Tag.of(name);
                            return tagRepository.save(newTag);
                        })
                )
                .collect(Collectors.toList());
    }
}
