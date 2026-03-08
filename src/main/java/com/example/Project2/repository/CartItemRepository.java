package com.example.Project2.repository;

import com.example.Project2.entity.Cart;
import com.example.Project2.entity.CartItem;
import com.example.Project2.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem , Integer> {

    Optional<CartItem> findByCartAndProduct(Cart cart , Product product);

    List<CartItem> findByCart(Cart cart);

    void deleteByCart(Cart cart);
}
