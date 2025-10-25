package namnt.vn.coffestore.ui.adapters;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SquareItemDecoration extends RecyclerView.ItemDecoration {
    
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, 
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        
        // Make items square by setting height equal to width
        int width = parent.getWidth() / 2; // 2 columns
        view.getLayoutParams().height = width;
    }
}
