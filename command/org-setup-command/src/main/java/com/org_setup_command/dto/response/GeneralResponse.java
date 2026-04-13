package com.org_setup_command.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneralResponse<T> {
	private T data;
	private String message;
	private ResponseStatus status;
}
