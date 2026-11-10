package com.devlog.core.repository.tag;

import com.devlog.core.entity.tag.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface TagRepository extends JpaRepository<Tag, Long>, TagRepositoryCustom {

    Boolean existsByTagName(String tagName);

    Optional<Tag> findByTagName(String tagName);

}
