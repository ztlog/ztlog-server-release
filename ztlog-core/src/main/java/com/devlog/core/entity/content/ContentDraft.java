package com.devlog.core.entity.content;

import com.devlog.core.entity.BaseTimeEntity;
import com.devlog.core.entity.category.Category;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "contents_draft")
public class ContentDraft extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DRAFT_NO", nullable = false)
    private Long draftNo;

    @Column(name = "CTNT_NO")
    private Long ctntNo;

    @Column(name = "CTNT_TITLE")
    private String ctntTitle;

    @Column(name = "CTNT_SUBTITLE")
    private String ctntSubTitle;

    @Column(name = "CTNT_BODY")
    private String ctntBody;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATE_NO")
    private Category category;

    @OneToMany(mappedBy = "drafts", fetch = FetchType.EAGER)
    @OrderBy("sort asc")
    private List<ContentDraftTag> draftTags = new ArrayList<>();

    @Column(name = "CTNT_PATH")
    private String ctntPath;

    @Column(name = "CTNT_NAME")
    private String ctntName;

    @Column(name = "CTNT_EXT")
    private String ctntExt;

    @Column(name = "INP_USER", nullable = false)
    private String inpUser;

    public static ContentDraft created(Long ctntNo, String title, String subTitle, String body, Category category,
                                        String path, String name, String ext, String inpUser) {
        return ContentDraft.builder()
                .ctntNo(ctntNo)
                .ctntTitle(title)
                .ctntSubTitle(subTitle)
                .ctntBody(body)
                .category(category)
                .ctntPath(path)
                .ctntName(name)
                .ctntExt(ext)
                .inpUser(inpUser)
                .build();
    }

    public void updated(String title, String subTitle, String body, Category category,
                         String path, String name, String ext, String inpUser) {
        this.ctntTitle = title;
        this.ctntSubTitle = subTitle;
        this.ctntBody = body;
        this.category = category;
        this.ctntPath = path;
        this.ctntName = name;
        this.ctntExt = ext;
        this.inpUser = inpUser;
    }

}