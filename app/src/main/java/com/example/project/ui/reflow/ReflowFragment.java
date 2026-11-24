package com.example.project.ui.reflow;

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

public class ReflowFragment extends Fragment {

    private DatabaseReference databaseReference;
    private ArrayList<String> cartCodes;
    private ArrayList<String> cartNames;
    private ArrayList<Integer> cartQuantities;
    private ArrayList<String> cartText;
    private ArrayAdapter<String> adapter;
    private EditText cartQuantityText;
    private int selectedPosition = -1;
    private String userId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_reflow, container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");

        cartCodes = new ArrayList<>();
        cartNames = new ArrayList<>();
        cartQuantities = new ArrayList<>();
        cartText = new ArrayList<>();

        ListView cartListView = root.findViewById(R.id.cartListView);
        cartQuantityText = root.findViewById(R.id.cartQuantityText);
        Button updateCartQuantityButton = root.findViewById(R.id.updateCartQuantityButton);
        Button confirmBuyButton = root.findViewById(R.id.confirmBuyButton);
        Button removeFromCartButton = root.findViewById(R.id.removeFromCartButton);

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, cartText);
        cartListView.setAdapter(adapter);

        cartListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < cartCodes.size()) {
                    selectedPosition = position;
                    cartQuantityText.setText(String.valueOf(cartQuantities.get(position)));
                }
            }
        });

        updateCartQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCartQuantity();
            }
        });

        confirmBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmBuy();
            }
        });

        removeFromCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeFromCart();
            }
        });

        showCart();
        return root;
    }

    private void showCart() {
        if (userId.isEmpty()) {
            cartText.clear();
            cartText.add("Please login again");
            adapter.notifyDataSetChanged();
            return;
        }

        databaseReference.child("Cart").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot cartSnapshot) {
                databaseReference.child("Products").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot productsSnapshot) {
                        cartCodes.clear();
                        cartNames.clear();
                        cartQuantities.clear();
                        cartText.clear();
                        selectedPosition = -1;
                        cartQuantityText.setText("");

                        for (DataSnapshot product : cartSnapshot.getChildren()) {
                            String code = getText(product, "code");
                            if (code.isEmpty()) {
                                code = product.getKey();
                            }

                            if (code == null || !productsSnapshot.child(code).exists()) {
                                product.getRef().removeValue();
                                continue;
                            }

                            int stock = getNumber(productsSnapshot.child(code), "quantity");
                            if (stock <= 0) {
                                product.getRef().removeValue();
                                continue;
                            }

                            String name = getText(product, "name");
                            int price = getNumber(product, "price");
                            int quantity = getNumber(product, "quantity");

                            cartCodes.add(code);
                            cartNames.add(name);
                            cartQuantities.add(quantity);
                            cartText.add("Code: " + code + "\nName: " + name + "\nPrice: " + price + "\nQuantity: " + quantity);
                        }

                        if (cartCodes.isEmpty()) {
                            cartText.add("Cart is empty");
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void removeFromCart() {
        if (selectedPosition == -1) {
            Toast.makeText(getContext(), "Choose product first", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child("Cart").child(userId).child(cartCodes.get(selectedPosition)).removeValue();
        Toast.makeText(getContext(), "Removed from cart", Toast.LENGTH_SHORT).show();
    }

    private void updateCartQuantity() {
        if (selectedPosition == -1) {
            Toast.makeText(getContext(), "Choose product first", Toast.LENGTH_SHORT).show();
            return;
        }

        String quantityString = cartQuantityText.getText().toString();
        if (quantityString.isEmpty()) {
            Toast.makeText(getContext(), "Enter quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = Integer.parseInt(quantityString);
        if (quantity <= 0) {
            Toast.makeText(getContext(), "Quantity must be bigger than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        String code = cartCodes.get(selectedPosition);
        databaseReference.child("Products").child(code).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot productSnapshot) {
                if (!productSnapshot.exists()) {
                    databaseReference.child("Cart").child(userId).child(code).removeValue();
                    Toast.makeText(getContext(), "Product was deleted", Toast.LENGTH_SHORT).show();
                    return;
                }

                int stock = getNumber(productSnapshot, "quantity");
                if (stock <= 0) {
                    databaseReference.child("Cart").child(userId).child(code).removeValue();
                    Toast.makeText(getContext(), "Product not in stock", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (quantity > stock) {
                    Toast.makeText(getContext(), "Not enough stock", Toast.LENGTH_SHORT).show();
                    return;
                }

                databaseReference.child("Cart").child(userId).child(code).child("quantity").setValue(quantity);
                Toast.makeText(getContext(), "Quantity updated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void confirmBuy() {
        if (cartCodes.isEmpty()) {
            Toast.makeText(getContext(), "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child("Products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (int i = 0; i < cartCodes.size(); i++) {
                    DataSnapshot productSnapshot = snapshot.child(cartCodes.get(i));
                    if (!productSnapshot.exists()) {
                        databaseReference.child("Cart").child(userId).child(cartCodes.get(i)).removeValue();
                        Toast.makeText(getContext(), "Product was deleted from cart", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int stock = getNumber(productSnapshot, "quantity");
                    if (stock <= 0) {
                        databaseReference.child("Cart").child(userId).child(cartCodes.get(i)).removeValue();
                        Toast.makeText(getContext(), "Product not in stock", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (stock < cartQuantities.get(i)) {
                        Toast.makeText(getContext(), "Not enough stock for " + cartNames.get(i), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                for (int i = 0; i < cartCodes.size(); i++) {
                    int stock = getNumber(snapshot.child(cartCodes.get(i)), "quantity");
                    int newStock = stock - cartQuantities.get(i);
                    databaseReference.child("Products").child(cartCodes.get(i)).child("quantity").setValue(newStock);
                    if (newStock <= 0) {
                        removeProductFromCarts(cartCodes.get(i));
                    }
                }

                databaseReference.child("Cart").child(userId).removeValue();
                Toast.makeText(getContext(), "Purchase confirmed", Toast.LENGTH_SHORT).show();
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



