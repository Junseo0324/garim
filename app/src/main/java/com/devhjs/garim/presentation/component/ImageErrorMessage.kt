package com.devhjs.garim.presentation.component
import com.devhjs.garim.domain.error.ImageError

fun ImageError.toMessage(): String = when (this) {
    ImageError.CANNOT_OPEN -> "사진을 열 수 없어요. 다른 사진을 선택해 주세요."
    ImageError.CANNOT_RENDER -> "결과를 만들지 못했어요. 다시 시도해 주세요."
    ImageError.CANNOT_SAVE -> "저장하지 못했어요. 저장 공간을 확인하고 다시 시도해 주세요."
    ImageError.ACCESS_LOST -> "사진 접근 권한이 없어졌어요. 사진을 다시 선택해 주세요."
    ImageError.INVALID_MASK -> "가릴 부분을 먼저 선택해 주세요."
    ImageError.NOT_FOUND -> "사진 파일을 찾을 수 없어요. 사진을 다시 선택해 주세요."
}
