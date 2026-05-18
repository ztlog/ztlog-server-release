package com.devlog.core.entity.content;

import com.devlog.core.entity.BaseTimeEntity;
import com.devlog.core.entity.category.Category;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.type.YesNoConverter;

import java.util.*;

@Getter
@Entity
@SoftDelete(columnName = "DELETED_YN", converter = YesNoConverter.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "contents_mst")
public class Content extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CTNT_NO", nullable = false)
    private Long ctntNo;

    @Column(name = "CTNT_TITLE", nullable = false)
    private String ctntTitle;

    @Column(name = "CTNT_SUBTITLE", nullable = false)
    private String ctntSubTitle;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATE_NO")
    private Category category;

    @OneToOne(mappedBy = "content", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn(name = "CTNT_NO")
    private ContentDetail contentDetail;

    @OneToMany(mappedBy = "contents", fetch = FetchType.EAGER)
    @OrderBy("sort asc")
    private List<ContentTag> contentTags = new ArrayList<>();

    @Column(name = "INP_USER", nullable = false)
    private String inpUser;

    public static Content created(String title, String subTitle, String body, Category category, String userId) {
        Content content = Content.builder()
                .ctntTitle(title)
                .ctntSubTitle(subTitle)
                .category(category)
                .inpUser(userId)
                .build();
        content.contentDetail = ContentDetail.created(title, body, userId, content);
        return content;
    }

    public void updated(String title, String subtitle, Category category, String inpUser) {
        this.ctntTitle = title;
        this.ctntSubTitle = subtitle;
        this.inpUser = inpUser;
        if (!Objects.isNull(category)) {
            this.category = category;
        }
    }

}
