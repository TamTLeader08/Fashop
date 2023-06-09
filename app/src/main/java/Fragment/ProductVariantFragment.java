package Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fashop.R;
import com.example.fashop.activity.AddModelActivity;
import com.example.fashop.activity.CartListActivity;
import com.example.fashop.activity.OrderActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Adapter.ColorAdapter;
import Adapter.SizeAdapter;
import Interface.OnColorClickListener;
import Interface.OnSizeClickListener;
import Model.CartItem;
import Model.ModelImage;
import Model.ProductModel;
import Model.ProductVariant;
//import MyClass.ManagementCart;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProductVariantFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProductVariantFragment extends BottomSheetDialogFragment implements OnSizeClickListener, OnColorClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Context context;

    private ImageView imgProduct;

    private ProductModel object;

    private ProductVariant selectedProductVariant;

    private TextView priceTxt, numberOrderTxt, addtoCartBtn;

    private ImageButton minusBtn, plusBtn, closeBtn;

    private boolean activatedAddcartBtn = false;

    int numberOrder = 1;

    String selectedSize, selectedColor;

    private RecyclerView recyclerViewSize, recyclerViewColor;

    private List<String> mListSize = new ArrayList<>();
    private List<String> mListColor = new ArrayList<>(Arrays.asList(
            "Red",
            "Green",
            "Blue",
            "Yellow",
            "Orange",
            "Purple",
            "Pink",
            "Brown",
            "Gray",
            "Black"
    ));

    private SizeAdapter sizeAdapter;

    private ColorAdapter colorAdapter;

//    private ManagementCart managementCart;

    private int selectedSizePosition = -1;

    private int selectedColorPosition = -1;

    private String typeButton;

    public ProductVariantFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProductVariantFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProductVariantFragment newInstance(String param1, String param2) {
        ProductVariantFragment fragment = new ProductVariantFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private void initUI(View view) {
        imgProduct = view.findViewById(R.id.imgProduct);
        priceTxt = view.findViewById(R.id.priceTxt);
        minusBtn = view.findViewById(R.id.minusBtn);
        plusBtn = view.findViewById(R.id.plusBtn);
        numberOrderTxt = view.findViewById(R.id.numberOrderTxt);
        addtoCartBtn = view.findViewById(R.id.addtoCartBtn);
        recyclerViewSize = view.findViewById(R.id.sizesRv);
        recyclerViewColor = view.findViewById(R.id.colorsRv);
        closeBtn = view.findViewById(R.id.closeBtn);

        numberOrderTxt.setText(String.valueOf(numberOrder));

        // Lấy đối tượng Bundle từ Fragment
        Bundle bundle = getArguments();
        if (bundle != null) {
            object = (ProductModel) bundle.getSerializable("object");
            typeButton = bundle.getString("typeButton");

            if (typeButton.equals("buyNow")){
                addtoCartBtn.setText("Buy Now");
            }
            // Sử dụng đối tượng object trong Fragment
        }

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // close fragment
                dismissAllowingStateLoss();
            }
        });

        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                numberOrder = numberOrder + 1;
                numberOrderTxt.setText(String.valueOf(numberOrder));
            }
        });

        minusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (numberOrder > 1){
                    numberOrder = numberOrder - 1;
                }
                numberOrderTxt.setText(String.valueOf(numberOrder));
            }
        });

        addtoCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activatedAddcartBtn = true;
                getSelectedVariantProduct();
            }
        });
        LoadImage();
        LoadColors();
        LoadSizes();
    }

    private void LoadImage(){
        try {
            Picasso.get().load(object.getImages().get(0)).placeholder(R.drawable.error).into(imgProduct);
        }
        catch (Exception e){
            imgProduct.setImageResource(R.drawable.error);
        }
        priceTxt.setText(String.valueOf(object.getPrice()));
    }

    private void LoadSizes(){
        GridLayoutManager manager = new GridLayoutManager(context, 8);

        // Adapter Category
        recyclerViewSize.setLayoutManager(manager);
        sizeAdapter = new SizeAdapter(context, mListSize, selectedSizePosition, colorAdapter);
        sizeAdapter.setOnSizeClickListener(this); // Set the listener
        recyclerViewSize.setAdapter(sizeAdapter);

        getSizeData();
    }
    private void getSizeData(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Variant");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mListSize != null){
                    mListSize.clear();
                }
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ProductVariant productVariant = dataSnapshot.getValue(ProductVariant.class);
                    if (productVariant != null && productVariant.getModelID() == object.getID()){
                        mListSize.add(productVariant.getSize());
                    }
                }

                Set<String> uniqueSizes = new HashSet<>(mListSize);
                mListSize.clear();
                mListSize.addAll(uniqueSizes);
                sizeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "get size list failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void LoadColors(){
        GridLayoutManager manager = new GridLayoutManager(context, 6);

        // Adapter Category
        recyclerViewColor.setLayoutManager(manager);
        colorAdapter = new ColorAdapter(context, mListColor, selectedColorPosition );
        colorAdapter.setOnColorClickListener(this); // Set the listener
        recyclerViewColor.setAdapter(colorAdapter);
    }

    private void getColorData(String selectedSize){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Variant");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mListColor != null){
                    mListColor.clear();
                }
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ProductVariant productVariant = dataSnapshot.getValue(ProductVariant.class);
                    if (productVariant != null && productVariant.getModelID() == object.getID() && productVariant.getSize().equals(String.valueOf(selectedSize))){
                        mListColor.add(productVariant.getColor());
                    }
                }

                Set<String> uniqueColors = new HashSet<>(mListColor);
                mListColor.clear();
                mListColor.addAll(uniqueColors);
                colorAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "get color list failed", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        context = getContext();
//        managementCart = new ManagementCart(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_product_variant, container, false);
        selectedSize = null;
        selectedColor = null;

        initUI(view);
        return view;
    }

    @Override
    public void onSizeClicked(String size, int position) {
        getColorData(size);
        selectedSize = size;
        activatedAddcartBtn = false;
        getSelectedVariantProduct();
    }

    @Override
    public void onColorClicked(String color, int position) {
        selectedColor = color;
        activatedAddcartBtn = false;
        getSelectedVariantProduct();
    }


    private void getSelectedVariantProduct() {

        if (selectedSize == null && activatedAddcartBtn == true){
            Toast.makeText(context, "Please select size", Toast.LENGTH_SHORT).show();
            activatedAddcartBtn = false;
        }
        else if (selectedColor == null && activatedAddcartBtn == true){
            Toast.makeText(context, "Please select color", Toast.LENGTH_SHORT).show();
            activatedAddcartBtn = false;
        }
        else if (selectedSize != null && selectedColor != null && activatedAddcartBtn == true) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Variant");

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        ProductVariant productVariant = dataSnapshot.getValue(ProductVariant.class);
                        if (productVariant != null
                                && productVariant.getModelID() == object.getID()
                                && productVariant.getSize().equals(String.valueOf(selectedSize))
                                && productVariant.getColor().equals(String.valueOf(selectedColor))){
                            selectedProductVariant = productVariant;
                            handleSelectedProductVariant(selectedProductVariant); // Gọi phương thức xử lý sau khi có selectedProductVariant
                            activatedAddcartBtn = false;
                            break;
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "get selected variant product failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void handleSelectedProductVariant(ProductVariant selectedProductVariant) {
//        object.setNumberInCart(numberOrder);
//        managementCart.insertVariantProduct(object);
//        Log.e("ProductVariantFragment", "Selected variant: " + selectedProductVariant.getID());
//        Log.v("ProductVariantFragment", "Selected variant: " + selectedProductVariant.getID());

        if (typeButton.equals("addToCart")){
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CartItem");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {


                    for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                        CartItem cartItem = categorySnapshot.getValue(CartItem.class);
                        if (cartItem != null && cartItem.getVariantID() == selectedProductVariant.getID()) {
                            int newQuantity = cartItem.getQuantity() + numberOrder;
                            ref.child(String.valueOf(cartItem.getID())).child("quantity").setValue(newQuantity,
                                    new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                            Toast.makeText(context, "Added To Your Cart", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            return;
                        }
                    }

                    int maxId = 0;
                    for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                        CartItem cartItem = categorySnapshot.getValue(CartItem.class);
                        if (cartItem != null && cartItem.getID() > maxId) {
                            maxId = cartItem.getID();
                        }
                    }

                    CartItem newCartItem = new CartItem();
                    newCartItem.setID(maxId + 1);
                    newCartItem.setQuantity(numberOrder);
                    newCartItem.setVariantID(selectedProductVariant.getID());
                    newCartItem.setCustomerID(FirebaseAuth.getInstance().getUid());

                    ref.child(String.valueOf(newCartItem.getID())).setValue(newCartItem,
                            new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                    Toast.makeText(context, "Added To Your Cart", Toast.LENGTH_SHORT).show();
                                }
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else if (typeButton.equals("buyNow")){
//            Thông tin cần lấy cho đơn hàng:
//            Quantity(numberOrder);
//            VariantID(selectedProductVariant.getID());
//            CustomerID(FirebaseAuth.getInstance().getUid());
//            Intent intent = new Intent(context, OrderActivity.class);
//            startActivity(intent);

            CartItem newCartItem = new CartItem();
            newCartItem.setID(0);
            newCartItem.setQuantity(numberOrder);
            newCartItem.setVariantID(selectedProductVariant.getID());
            newCartItem.setCustomerID(FirebaseAuth.getInstance().getUid());
            newCartItem.setSize(selectedProductVariant.getSize());
            newCartItem.setColor(selectedProductVariant.getColor());

            DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("Model" + "/" + selectedProductVariant.getModelID());
            ref2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ProductModel productModel = snapshot.getValue(ProductModel.class);

                    if (productModel != null){
                        //get color size
                        Double price = productModel.getPrice();
                        newCartItem.setPrice(price);

                        String title = productModel.getName();
                        newCartItem.setProductName(title);

                        DatabaseReference ref3 = FirebaseDatabase.getInstance().getReference("ModelImage");
                        ref3.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                    ModelImage modelImage = dataSnapshot.getValue(ModelImage.class);
                                    if (modelImage != null && modelImage.getModelID() == selectedProductVariant.getModelID()){
                                        newCartItem.setImage(modelImage.getUrl());
                                        break;
                                    }
                                }

                                List<CartItem> cartItemList = new ArrayList<>();
                                cartItemList.add(newCartItem);

                                Intent intent = new Intent(context, OrderActivity.class);
                                intent.putExtra("cart_items_list_key", new Gson().toJson(cartItemList));
                                intent.putExtra("total_key", newCartItem.getPrice());
                                startActivity(intent);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(context, "get size list failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "get size list failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}