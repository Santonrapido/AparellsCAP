package cat.ics.aparellscap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EquipoAdapter extends RecyclerView.Adapter<EquipoAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(EquipoItem item);
    }

    private List<EquipoItem> items;
    private OnItemClickListener listener;

    public EquipoAdapter(List<EquipoItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_equipo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EquipoItem item = items.get(position);
        holder.tvNombre.setText(item.nombre);
        holder.tvInfo.setText(item.tipo + "  •  ID: " + item.id);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvInfo;
        ViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvInfo = itemView.findViewById(R.id.tvInfo);
        }
    }

    // Clase para los datos
    public static class EquipoItem {
        public int id;
        public String nombre;
        public String tipo;

        public EquipoItem(int id, String nombre, String tipo) {
            this.id = id;
            this.nombre = nombre;
            this.tipo = tipo;
        }
    }
}
