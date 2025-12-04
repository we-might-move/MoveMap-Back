package org.wemightmove.movemap.domain.video.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exercise_video")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExerciseVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "category_large", length = 50, nullable = false)
    private String categoryLarge;

    @Column(name = "category_middle", length = 100, nullable = false)
    private String categoryMiddle;

    @Column(name = "category_small", length = 100, nullable = false)
    private String categorySmall;

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Column(name = "youtube_url", nullable = false)
    private String youtubeUrl;

    @Column(name = "youtube_video_id", length = 50, nullable = false, unique = true)
    private String youtubeVideoId;

    // CSV에서 적재할 때만 쓸 생성자 (애플리케이션에서 직접 생성할 일은 거의 없음)
    public ExerciseVideo(
            Long id,
            String categoryLarge,
            String categoryMiddle,
            String categorySmall,
            String title,
            String youtubeUrl,
            String youtubeVideoId
    ) {
        this.id = id;
        this.categoryLarge = categoryLarge;
        this.categoryMiddle = categoryMiddle;
        this.categorySmall = categorySmall;
        this.title = title;
        this.youtubeUrl = youtubeUrl;
        this.youtubeVideoId = youtubeVideoId;
    }
}