package com.cloudtheon.knowflowcore.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 通用分页 VO
 *
 * @param records  当前页数据
 * @param total    总记录数
 * @param page     当前页码
 * @param pageSize 每页记录数
 */
@Schema(description = "分页数据")
public record PageVO<T>(
        @Schema(description = "当前页数据") List<T> records,
        @Schema(description = "总记录数") long total,
        @Schema(description = "当前页码") long page,
        @Schema(description = "每页记录数") long pageSize) {
}
