package atomsandbots.android.honey.user.UI;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import atomsandbots.android.honey.user.Adapter.HomeAdapter;
import atomsandbots.android.honey.user.Extras.GridSpacingItemDecoration;
import atomsandbots.android.honey.user.Model.ProductModel;
import atomsandbots.android.honey.user.R;

public class HomeFragment extends Fragment {

    private RecyclerView homeRecyclerView;
    private List<ProductModel> productModelList;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.home_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.termcondition) {
            //Code For Terms And condition
            return true;
        }
        return false;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        ConnectivityManager conMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
        if (netInfo == null) {
            Toast.makeText(getContext(), "Check internet Connection", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Please Wait");
            progressDialog.show();
            homeRecyclerView = root.findViewById(R.id.home_recyclerview);

            productModelList = new ArrayList<>();
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
            homeRecyclerView.setLayoutManager(gridLayoutManager);
            int spanCount = 2; // 3 columns
            int spacing = 15; // 50px
            boolean includeEdge = false;
            homeRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, false));
            homeRecyclerView.setHasFixedSize(true);
            homeRecyclerView.setItemViewCacheSize(20);
            homeRecyclerView.setDrawingCacheEnabled(true);
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Products");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    productModelList.clear();
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        ProductModel product = snapshot1.getValue(ProductModel.class);
                        productModelList.add(product);
                    }
                    HomeAdapter homeAdapter = new HomeAdapter(productModelList, getContext(), true, false,false);
                    homeRecyclerView.setAdapter(homeAdapter);
                    progressDialog.dismiss();
                    homeAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        return root;
    }
}