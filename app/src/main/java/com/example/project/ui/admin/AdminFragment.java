package com.example.project.ui.admin;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.project.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminFragment extends Fragment {

    private DatabaseReference databaseReference;
    private ArrayList<String> productCodes;
    private ArrayList<String> productNames;
    private ArrayList<Integer> productPrices;
    private ArrayList<Integer> productQuantities;
    private ArrayList<String> productsText;
    private ArrayAdapter<String> adapter;
    private EditText codeText, nameText, priceText, quantityText;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin, container, false);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("isAdmin", false)) {
            Toast.makeText(getContext(), "Admin only", Toast.LENGTH_SHORT).show();
            return root;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();
        productCodes = new ArrayList<>();
        productNames = new ArrayList<>();
        productPrices = new ArrayList<>();
        productQuantities = new ArrayList<>();
        productsText = new ArrayList<>();

        ListView listView = root.findViewById(R.id.adminProductsListView);
        codeText = root.findViewById(R.id.productCodeText);
        nameText = root.findViewById(R.id.productNameText);
        priceText = root.findViewById(R.id.productPriceText);
        quantityText = root.findViewById(R.id.productQuantityText);
        Button addButton = root.findViewById(R.id.addProductButton);
        Button updateButton = root.findViewById(R.id.updateProductButton);
        Button deleteButton = root.findViewById(R.id.deleteProductButton);

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, productsText);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                codeText.setText(productCodes.get(position));
                nameText.setText(productNames.get(position));
                priceText.setText(String.valueOf(productPrices.get(position)));
                quantityText.setText(String.valueOf(productQuantities.get(position)));
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProduct();
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProduct();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteProduct();
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

                for (DataSnapshot product : snapshot.getChildren()) {
                    String code = getText(product, "code");
                    String name = getText(product, "name");
                    int price = getNumber(product, "price");
                    int quantity = getNumber(product, "quantity");

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

    private void saveProduct() {
        String code = codeText.getText().toString();
        String name = nameText.getText().toString();
        String priceString = priceText.getText().toString();
        String quantityString = quantityText.getText().toString();

        if (code.isEmpty() || name.isEmpty() || priceString.isEmpty() || quantityString.isEmpty()) {
            Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int price = Integer.parseInt(priceString);
        int quantity = Integer.parseInt(quantityString);

        if (quantity < 0) {
            Toast.makeText(getContext(), "Quantity cannot be negative", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child("Products").child(code).child("code").setValue(code);
        databaseReference.child("Products").child(code).child("name").setValue(name);
        databaseReference.child("Products").child(code).child("price").setValue(price);
        databaseReference.child("Products").child(code).child("quantity").setValue(quantity);
        if (quantity == 0) {
            removeProductFromCarts(code);
        }

        Toast.makeText(getContext(), "Product saved", Toast.LENGTH_SHORT).show();
    }


    private void updateProduct() {
        String code = codeText.getText().toString();
        String name = nameText.getText().toString();
        String priceString = priceText.getText().toString();
        String quantityString = quantityText.getText().toString();

        if (code.isEmpty() || name.isEmpty() || priceString.isEmpty() || quantityString.isEmpty()) {
            Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int price = Integer.parseInt(priceString);
        int quantity = Integer.parseInt(quantityString);

        if (quantity < 0) {
            Toast.makeText(getContext(), "Quantity cannot be negative", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child("Products").child(code).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(getContext(), "Product does not exist", Toast.LENGTH_SHORT).show();
                    return;
                }

                databaseReference.child("Products").child(code).child("code").setValue(code);
                databaseReference.child("Products").child(code).child("name").setValue(name);
                databaseReference.child("Products").child(code).child("price").setValue(price);
                databaseReference.child("Products").child(code).child("quantity").setValue(quantity);
                if (quantity == 0) {
                    removeProductFromCarts(code);
                }

                Toast.makeText(getContext(), "Product updated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void deleteProduct() {
        String code = codeText.getText().toString();

        if (code.isEmpty()) {
            Toast.makeText(getContext(), "Choose product first", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child("Products").child(code).removeValue();
        removeProductFromCarts(code);
        clearFields();
        Toast.makeText(getContext(), "Product deleted", Toast.LENGTH_SHORT).show();
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

    private void clearFields() {
        codeText.setText("");
        nameText.setText("");
        priceText.setText("");
        quantityText.setText("");
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




