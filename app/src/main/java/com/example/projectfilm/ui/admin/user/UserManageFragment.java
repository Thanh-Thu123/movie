package com.example.projectfilm.ui.admin.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projectfilm.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserManageFragment extends Fragment {

    private ListView userListView;
    private Button btnDeleteUser;
    private EditText etSearchUser;
    private ImageButton btnSearchUser;
    private ArrayAdapter<String> userAdapter;
    private List<String> userList = new ArrayList<>();
    private List<String> fullUserList = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.admin_manage_users, container, false);

        // Ánh xạ View
        userListView = rootView.findViewById(R.id.list_view_user);
        etSearchUser = rootView.findViewById(R.id.et_search_user);
        btnSearchUser = rootView.findViewById(R.id.btn_search_user);
        btnDeleteUser = rootView.findViewById(R.id.btn_delete_user);
        ImageButton btnBack = rootView.findViewById(R.id.btn_back); // Nút quay lại

        db = FirebaseFirestore.getInstance();

        // Thiết lập Adapter
        userAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_single_choice, userList);
        userListView.setAdapter(userAdapter);
        userListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        loadUserData();

        // Tìm kiếm
        btnSearchUser.setOnClickListener(v -> {
            String keyword = etSearchUser.getText().toString().trim();
            filterUsers(keyword);
        });

        // ✅ Sửa ở đây: xử lý nút back
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Xử lý xóa người dùng
        btnDeleteUser.setOnClickListener(v -> {
            int selectedPosition = userListView.getCheckedItemPosition();
            if (selectedPosition != ListView.INVALID_POSITION) {
                String selectedItem = userList.get(selectedPosition);
                String name = selectedItem.split(" - ")[0].trim(); // Lấy tên để tìm
                deleteUserByName(name);
            } else {
                Toast.makeText(requireContext(), "Vui lòng chọn người dùng để xóa!", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    private void loadUserData() {
        db.collection("users").addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(requireContext(), "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            fullUserList.clear();
            userList.clear();

            if (value != null && !value.isEmpty()) {
                for (DocumentSnapshot document : value) {
                    String name = document.getString("name");
                    String email = document.getString("email");

                    if (name != null && email != null) {
                        String display = name + " - " + email;
                        fullUserList.add(display);
                        userList.add(display);
                    }
                }
                userAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(requireContext(), "Không có người dùng nào!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterUsers(String query) {
        userList.clear();
        for (String user : fullUserList) {
            if (user.toLowerCase().contains(query.toLowerCase())) {
                userList.add(user);
            }
        }
        userAdapter.notifyDataSetChanged();
    }

    private void deleteUserByName(String name) {
        db.collection("users")
                .whereEqualTo("name", name)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    for (DocumentSnapshot document : querySnapshots) {
                        db.collection("users").document(document.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(requireContext(), "Xóa thành công!", Toast.LENGTH_SHORT).show();
                                    loadUserData(); // Reload sau khi xóa
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireContext(), "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Lỗi truy vấn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
