package com.xianguoji.server.module.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class FeedbackAddDto {

    private String type;
    @NotBlank(message = "内容不能为空")
    @Size(max = 1000)
    private String content;
    private List<String> images;
    private String contact;
}
