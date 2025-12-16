package com.yaned.final_2025merino.api;

import androidx.annotation.Nullable;

import com.yaned.final_2025merino.api.dto.*;
import com.yaned.final_2025merino.api.dto.InventarioRegistroDTO; // import explícito

import java.io.IOException;
import java.util.List;
import java.util.Map;

import retrofit2.Response;
import okhttp3.ResponseBody; // agregado para manejar errorBody

public class ApiRepository {
    private final ApiService service;

    public ApiRepository() {
        this.service = ApiClient.get().create(ApiService.class);
    }

    // Helper para leer el cuerpo de error cerrando correctamente el ResponseBody
    private String readErrorBody(Response<?> r) {
        try {
            ResponseBody rb = r.errorBody();
            if (rb == null) return null;
            try (ResponseBody ignored = rb) {
                return rb.string();
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public List<ProductoDTO> listarProductos() throws IOException {
        Response<List<ProductoDTO>> r = service.listarProductos().execute();
        if (!r.isSuccessful()) {
            String body = readErrorBody(r);
            throw new IOException(body != null && !body.isEmpty() ? body : ("HTTP " + r.code()));
        }
        return r.body();
    }

    @Nullable
    public List<CategoriaDTO> listarCategorias() throws IOException {
        Response<List<CategoriaDTO>> r = service.listarCategorias().execute();
        return r.isSuccessful() ? r.body() : null;
    }

    @Nullable
    public List<AlmacenDTO> listarAlmacenes() throws IOException {
        Response<List<AlmacenDTO>> r = service.listarAlmacenes().execute();
        return r.isSuccessful() ? r.body() : null;
    }

    @Nullable
    public StockResponse obtenerStock(int productoId, int almacenId) throws IOException {
        Response<StockResponse> r = service.obtenerStock(productoId, almacenId).execute();
        return r.isSuccessful() ? r.body() : null;
    }

    @Nullable
    public BasicResponse registrarMovimiento(int inventarioId, String tipo, int cantidad, String referencia, String comentario) throws IOException {
        Response<BasicResponse> r = service.registrarMovimiento(inventarioId, tipo, cantidad, referencia, comentario).execute();
        return r.isSuccessful() ? r.body() : null;
    }

    @Nullable
    public Integer obtenerOCrearInventarioId(int productoId, int almacenId) throws IOException {
        Response<InventarioIdResponse> r = service.obtenerOCrearInventario(productoId, almacenId).execute();
        if (!r.isSuccessful() || r.body() == null) return null;
        return r.body().inventario_id;
    }

    @Nullable
    public BasicResponse agregarProducto(String sku, String nombre, String descripcion, double precio, Integer categoriaId) throws IOException {
        Response<BasicResponse> r = service.agregarProducto(sku, nombre, descripcion, precio, categoriaId).execute();
        if (!r.isSuccessful()) {
            BasicResponse br = new BasicResponse();
            br.success = false;
            String body = readErrorBody(r);
            br.msg = body != null && !body.isEmpty() ? body : ("HTTP " + r.code());
            return br;
        }
        return r.body();
    }

    @Nullable
    public BasicResponse agregarCategoria(String nombre, String descripcion) throws IOException {
        Response<BasicResponse> r = service.agregarCategoria(nombre, descripcion).execute();
        if (!r.isSuccessful()) {
            BasicResponse br = new BasicResponse();
            br.success = false;
            String body = readErrorBody(r);
            br.msg = body != null && !body.isEmpty() ? body : ("HTTP " + r.code());
            return br;
        }
        return r.body();
    }

    @Nullable
    public BasicResponse agregarAlmacen(String nombre, String ubicacion) throws IOException {
        Response<BasicResponse> r = service.agregarAlmacen(nombre, ubicacion).execute();
        if (!r.isSuccessful()) {
            BasicResponse br = new BasicResponse();
            br.success = false;
            String body = readErrorBody(r);
            br.msg = body != null && !body.isEmpty() ? body : ("HTTP " + r.code());
            return br;
        }
        return r.body();
    }

    @Nullable
    public BasicResponse registrarIngreso(int almacenId, String referencia, String comentario, String itemsJson) throws IOException {
        Response<BasicResponse> r = service.registrarIngreso(almacenId, referencia, comentario, itemsJson).execute();
        if (!r.isSuccessful()) {
            BasicResponse br = new BasicResponse();
            br.success = false;
            String body = readErrorBody(r);
            br.msg = body != null && !body.isEmpty() ? body : ("HTTP " + r.code());
            return br;
        }
        return r.body();
    }

    @Nullable
    public BasicResponse guardarInventarioRegistros(String inventarioRef, Integer almacenId, List<Map<String, Object>> items) throws IOException {
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        if (inventarioRef != null) payload.put("inventario_ref", inventarioRef);
        if (almacenId != null) payload.put("almacen_id", almacenId);
        payload.put("items", items);
        Response<BasicResponse> r = service.guardarInventarioRegistros(payload).execute();
        if (!r.isSuccessful()) {
            BasicResponse br = new BasicResponse();
            br.success = false;
            String body = readErrorBody(r);
            br.msg = body != null && !body.isEmpty() ? body : ("HTTP " + r.code());
            return br;
        }
        return r.body();
    }

    @Nullable
    public List<InventarioDTO> listarInventarios() throws IOException {
        Response<List<InventarioDTO>> r = service.listarInventarios().execute();
        return r.isSuccessful() ? r.body() : null;
    }

    @Nullable
    public com.yaned.final_2025merino.api.dto.LoginResponse login(String correo, String clave) throws IOException {
        Response<com.yaned.final_2025merino.api.dto.LoginResponse> r = service.login(correo, clave).execute();
        return r.isSuccessful() ? r.body() : null;
    }

    @Nullable
    public List<InventarioRegistroDTO> listarInventarioRegistros() throws IOException {
        Response<List<InventarioRegistroDTO>> r = service.listarInventarioRegistros().execute();
        return r.isSuccessful() ? r.body() : null;
    }
}
