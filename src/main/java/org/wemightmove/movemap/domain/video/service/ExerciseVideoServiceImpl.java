package org.wemightmove.movemap.domain.video.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wemightmove.movemap.domain.video.dto.response.VideoCodeResponse;
import org.wemightmove.movemap.domain.video.entity.ExerciseVideo;
import org.wemightmove.movemap.domain.video.repository.ExerciseVideoRepository;
import org.wemightmove.movemap.global.exception.CustomException;
import org.wemightmove.movemap.global.exception.ErrorCode;

import java.security.SecureRandom;
import java.util.Random;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseVideoServiceImpl implements ExerciseVideoService{

    private final ExerciseVideoRepository exerciseVideoRepository;

    @Override
    public VideoCodeResponse getRandomVideoCode() {

        long count = exerciseVideoRepository.count();

        Random random = new SecureRandom();

        int index = random.nextInt((int) count);
        Page<ExerciseVideo> page = exerciseVideoRepository.findAll(PageRequest.of(index, 1));

        if (page.isEmpty()) {
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }

        return new VideoCodeResponse(page.getContent().get(0).getId(), page.getContent().get(0).getYoutubeVideoId());
    }
}
