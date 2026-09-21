package com.devlog.core.repository.content;

import com.devlog.core.entity.content.ContentDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentDraftRepository extends JpaRepository<ContentDraft, Long> {

}