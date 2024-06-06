package ru.mvideo.handoveroptionavailability.processor.filter.fit;

import lombok.Data;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.product.model.ProductDto;

@Data
public class ExtendedProduct {
	private ProductDto product;
	private int qty;
	private Double price;

	public ExtendedProduct() {
	}

	public ExtendedProduct(ProductDto product, Material material) {
		this.product = product;
		this.qty = material.getQty();
		this.price = material.getPrice();
	}

	public ExtendedProduct(ProductDto product, int qty) {
		this.product = product;
		this.qty = qty;
	}

	public ExtendedProduct(Material material) {
		final var productDto = new ProductDto();
		productDto.setProductId(material.getMaterial());
		this.product = productDto;
		this.qty = material.getQty();
		this.price = material.getPrice();
	}
}
