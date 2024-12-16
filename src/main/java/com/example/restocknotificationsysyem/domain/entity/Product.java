package com.example.restocknotificationsysyem.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Product extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    private Long id;

    @Column(name = "restock_round", nullable = false)
    private int restockRound;

    @Column(name = "stock", nullable = false)
    private int stock;

    @Column
    private int previousStock;

    public void increaseRestockRound() {
        this.restockRound += 1;
    }


    public void decreaseStock() {
        if (this.stock > 0) {
            this.stock -= 1;
        }
    }

    public boolean isOutOfStock() {
        return stock <= 0;
    }

    public void updatePreviousStock(int stock) {
        this.previousStock = stock;
    }

    /**
     * 🔥 재고 추가 메서드 (세터를 대체)
     * @param quantity 추가할 수량
     */
    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("추가할 수량은 0보다 커야 합니다.");
        }
        this.stock += quantity;
    }


}
