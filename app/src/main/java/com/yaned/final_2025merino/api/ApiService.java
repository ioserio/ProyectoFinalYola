package com.yaned.final_2025merino.api;

import com.yaned.final_2025merino.api.dto.*;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    // Usuarios
    @FormUrlEncoded
    @POST("login.php")
    Call<LoginResponse> login(@Field("correo") String correo, @Field("clave") String clave);

    @FormUrlEncoded
    @POST("registrar_usuario.php")
    Call<BasicResponse> registrarUsuario(@Field("nombre") String nombre, @Field("correo") String correo,
                                         @Field("clave") String clave, @Field("rol") String rol);

    @GET("listar_usuarios.php")
    Call<List<UsuarioDTO>> listarUsuarios();

    @FormUrlEncoded
    @POST("actualizar_usuario.php")
    Call<BasicResponse> actualizarUsuario(@Field("id") int id, @Field("nombre") String nombre, @Field("rol") String rol);

    @FormUrlEncoded
    @POST("eliminar_usuario.php")
    Call<BasicResponse> eliminarUsuario(@Field("id") int id);

    // Categorías
    @GET("listar_categorias.php")
    Call<List<CategoriaDTO>> listarCategorias();

    @FormUrlEncoded
    @POST("agregar_categoria.php")
    Call<BasicResponse> agregarCategoria(@Field("nombre") String nombre, @Field("descripcion") String descripcion);

    // Almacenes
    @GET("listar_almacenes.php")
    Call<List<AlmacenDTO>> listarAlmacenes();

    @FormUrlEncoded
    @POST("agregar_almacen.php")
    Call<BasicResponse> agregarAlmacen(@Field("nombre") String nombre, @Field("ubicacion") String ubicacion);

    // Productos
    @GET("listar_productos.php")
    Call<List<ProductoDTO>> listarProductos();

    @FormUrlEncoded
    @POST("agregar_producto.php")
    Call<BasicResponse> agregarProducto(@Field("sku") String sku, @Field("nombre") String nombre,
                                        @Field("descripcion") String descripcion, @Field("precio") double precio,
                                        @Field("categoria_id") Integer categoriaId);

    @FormUrlEncoded
    @POST("actualizar_producto.php")
    Call<BasicResponse> actualizarProducto(@Field("id") int id, @Field("nombre") String nombre,
                                           @Field("descripcion") String descripcion, @Field("precio") double precio,
                                           @Field("categoria_id") Integer categoriaId);

    @FormUrlEncoded
    @POST("eliminar_producto.php")
    Call<BasicResponse> eliminarProducto(@Field("id") int id);

    // Inventarios
    @GET("obtener_stock.php")
    Call<StockResponse> obtenerStock(@Query("producto_id") int productoId, @Query("almacen_id") int almacenId);

    @FormUrlEncoded
    @POST("registrar_movimiento.php")
    Call<BasicResponse> registrarMovimiento(@Field("inventario_id") int inventarioId,
                                            @Field("tipo") String tipo,
                                            @Field("cantidad") int cantidad,
                                            @Field("referencia") String referencia,
                                            @Field("comentario") String comentario);

    // Inventarios: obtener o crear por producto/almacén (requiere PHP en hosting)
    @FormUrlEncoded
    @POST("obtener_o_crear_inventario.php")
    Call<InventarioIdResponse> obtenerOCrearInventario(@Field("producto_id") int productoId,
                                                       @Field("almacen_id") int almacenId);

    @FormUrlEncoded
    @POST("registrar_ingreso.php")
    Call<BasicResponse> registrarIngreso(
            @Field("almacen_id") int almacenId,
            @Field("referencia") String referencia,
            @Field("comentario") String comentario,
            @Field("items") String itemsJson
    );

    @GET("listar_inventarios.php")
    Call<List<InventarioDTO>> listarInventarios();

    // Inventarios: enviar registros de inventario en lote (JSON)
    @Headers("Content-Type: application/json")
    @POST("guardar_inventario_registros.php")
    Call<BasicResponse> guardarInventarioRegistros(@Body Map<String, Object> payload);

    @GET("listar_inventario_registros.php")
    Call<List<InventarioRegistroDTO>> listarInventarioRegistros();
}
