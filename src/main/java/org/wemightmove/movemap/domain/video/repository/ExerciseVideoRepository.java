package org.wemightmove.movemap.domain.video.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wemightmove.movemap.domain.video.entity.ExerciseVideo;

public interface ExerciseVideoRepository extends JpaRepository<ExerciseVideo, Long> {
}