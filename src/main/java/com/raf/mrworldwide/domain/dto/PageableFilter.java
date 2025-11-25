package com.raf.mrworldwide.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@Data
public class PageableFilter {
	protected Integer page;
	protected Integer size;
	protected Sort.Direction sortDirection;
	protected List<String> sortProperty;

	@JsonIgnore
	public Pageable getPageable() {
		if (page == null) {
			page = 0;
		}
		if (size == null) {
			size = 20;
		}
		if (sortDirection != null && sortProperty != null) {
			Sort sort = Sort.by(sortDirection, sortProperty.toArray(new String[]{}));
			return PageRequest.of(page, size, sort);
		} else {
			return PageRequest.of(page, size);
		}
	}
}
