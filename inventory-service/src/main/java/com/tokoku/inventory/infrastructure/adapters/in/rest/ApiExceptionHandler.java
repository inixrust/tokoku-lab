package com.tokoku.inventory.infrastructure.adapters.in.rest;

import java.net.URI;

import com.tokoku.inventory.domain.exception.StockNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(StockNotFoundException.class)
	ProblemDetail handleNotFound(StockNotFoundException ex) {
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
				"No stock record for product " + ex.productId());
		pd.setType(URI.create("https://api.tokoku.example/problems/stock-not-found"));
		pd.setTitle("Stock not found");
		pd.setProperty("errorCode", "STOCK_NOT_FOUND");
		pd.setProperty("productId", ex.productId());
		return pd;
	}

}
