package com.example.project.ui.transform;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.project.Home;
import com.example.project.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TransformFragment extends Fragment {

    private DatabaseReference databaseReference;
    private ArrayList<String> productCodes;
    private ArrayList<String> productNames;
    private ArrayList<Integer> productPrices;
    private ArrayList<Integer> productQuantities;
    private ArrayList<String> productsText;
    private ArrayAdapter<String> adapter;
    private int selectedPosition = -1;
    private String userId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_transform, container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");

        productCodes = new ArrayList<>();
        productNames = new ArrayList<>();
        productPrices = new ArrayList<>();
        productQuantities = new ArrayList<>();
        productsText = new ArrayList<>();

        ListView productsListView = root.findViewById(R.id.productsListView);
        Button addToCartButton = root.findViewById(R.id.addToCartButton);
        Button cartPageButton = root.findViewById(R.id.cartPageButton);

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, productsText);
        productsListView.setAdapter(adapter);

        productsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPosition = position;
            }
        });

        addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToCart();
            }
        });

        cartPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Home) requireActivity()).openPage(R.id.nav_reflow);
            }
        });

        showProducts();
        return root;
    }

    private void showProducts() {
        databaseReference.child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productCodes.clear();
                productNames.clear();
                productPrices.clear();
                productQuantities.clear();
                productsText.clear();
                selectedPosition = -1;

                for (DataSnapshot product : snapshot.getChildren()) {
                    String code = getText(product, "code");
                    if (code.isEmpty()) {
                        code = product.getKey();
                    }
                    String name = getText(product, "name");
                    int price = getNumber(product, "price");
                    int quantity = getNumber(product, "quantity");

                    if (code == null || quantity <= 0) {
                        if (code != null) {
                            removeProductFromCarts(code);
                        }
                        continue;
                    }

                    productCodes.add(code);
                    productNames.add(name);
                    productPrices.add(price);
                    productQuantities.add(quantity);
                    productsText.add("Code: " + code + "\nName: " + name + "\nPrice: " + price + "\nQuantity: " + quantity);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void addToCart() {
        if (selectedPosition == -1) {
            Toast.makeText(getContext(), "Choose product first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (productQuantities.get(selectedPosition) <= 0) {
            Toast.makeText(getContext(), "Product not in stock", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userId.isEmpty()) {
            Toast.makeText(getContext(), "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        String code = productCodes.get(selectedPosition);
        String name = productNames.get(selectedPosition);
        int price = productPrices.get(selectedPosition);
        int stockQuantity = productQuantities.get(selectedPosition);

        DatabaseReference cartProduct = databaseReference.child("Cart").child(userId).child(code);
        cartProduct.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int cartQuantity = getNumber(snapshot, "quantity");

                if (cartQuantity + 1 > stockQuantity) {
                    Toast.makeText(getContext(), "Not enough stock", Toast.LENGTH_SHORT).show();
                    return;
                }

                cartProduct.child("code").setValue(code);
                cartProduct.child("name").setValue(name);
                cartProduct.child("price").setValue(price);
                cartProduct.child("quantity").setValue(cartQuantity + 1);

                Toast.makeText(getContext(), "Added to cart", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void removeProductFromCarts(String code) {
        databaseReference.child("Cart").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userCart : snapshot.getChildren()) {
                    userCart.child(code).getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private String getText(DataSnapshot snapshot, String key) {
        String value = snapshot.child(key).getValue(String.class);
        if (value == null) {
            return "";
        }
        return value;
    }

    private int getNumber(DataSnapshot snapshot, String key) {
        Long value = snapshot.child(key).getValue(Long.class);
        if (value == null) {
            return 0;
        }
        return value.intValue();
    }
}


