package org.architect.multitenantappointmentsystem.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDto<T> {
    private Boolean success;
    private String message;
    private Integer count;
    private Long totalCount;
    private T data;
    @JsonIgnore
    private Integer statusCode;

    public static <T> ResponseDto<T> ok(T data) {
        ResponseDto<T> dto = new ResponseDto<>();
        dto.setSuccess(true);
        dto.setData(data);
        return dto;
    }
    public static ResponseDto<Void> ok() {
        ResponseDto<Void> dto = new ResponseDto<>();
        dto.setSuccess(true);
        return dto;
    }

    public static <T> ResponseDto<T> ok(T data, String message) {
        ResponseDto<T> dto = ok(data);
        dto.setMessage(message);
        return dto;
    }


    public static <I> ResponseDto<List<I>> ok(Page<I> page) {
        ResponseDto<List<I>> dto = new ResponseDto<>();
        dto.setSuccess(true);
        dto.setData(page.getContent());
        dto.setCount(page.getNumberOfElements());
        dto.setTotalCount(page.getTotalElements());
        return dto;
    }
    public static ResponseDto unauthorized(){
        ResponseDto responseDto= new ResponseDto();
        responseDto.setStatusCode(401);
        return responseDto;
    }

    public ResponseEntity<ResponseDto<T>> toResponseEntity() {
        int statusCode = Optional.ofNullable(this.statusCode).orElse(200);
        return ResponseEntity.status(statusCode).body(this);
    }



}

