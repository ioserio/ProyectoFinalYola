package com.yaned.final_2025merino.api;

import androidx.annotation.Nullable;

import com.yaned.final_2025merino.api.dto.*;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;

public class ApiRepository {
    private final ApiService service;

    public ApiRepository() {
        this.service = ApiClient.get().create(ApiService.class);
    }

    @Nullable
    public List<ProductoDTO> listarProductos() throws IOException {
        Response<List<ProductoDTO>> r = service.listarProductos().execute();
        if (!r.isSuccessful()) {
            String body = null;
            try { body = r.errorBody() != null ? r.errorBody().string() : null; } catch (Exception ignored) {}
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
            String body = null;
            try { body = r.errorBody() != null ? r.errorBody().string() : null; } catch (Exception ignored) {}
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
            String body = null;
            try { body = r.errorBody() != null ? r.errorBody().string() : null; } catch (Exception ignored) {}
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
            String body = null;
            try { body = r.errorBody() != null ? r.errorBody().string() : null; } catch (Exception ignored) {}
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
            String body = null;
            try { body = r.errorBody() != null ? r.errorBody().string() : null; } catch (Exception ignored) {}
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
}
