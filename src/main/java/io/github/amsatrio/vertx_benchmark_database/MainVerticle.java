package io.github.amsatrio.vertx_benchmark_database;

import java.util.ArrayList;
import java.util.List;

import io.github.amsatrio.vertx_benchmark_database.dto.request.AppResponse;
import io.github.amsatrio.vertx_benchmark_database.modules.conditions.Conditions;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;

public class MainVerticle extends VerticleBase {

  private Pool pool = null;

  @Override
  public Future<?> start() {
    ConfigStoreOptions configStoreOptions = new ConfigStoreOptions()
        .setType("file")
        .setFormat("yaml")
        .setConfig(new JsonObject().put("path", "application.yaml"));
    ConfigRetriever retriever = ConfigRetriever.create(vertx,
        new io.vertx.config.ConfigRetrieverOptions().addStore(configStoreOptions));

    return retriever.getConfig()
        .onFailure(cause -> {
          System.err.println("Failed to load configuration: " + cause.getMessage());
        })
        .compose(jsonObject -> {
          JsonObject serverConfig = jsonObject.getJsonObject("server", new JsonObject());
          int serverPort = serverConfig.getInteger("port", 8888);

          pool = getPostgresqlPool(jsonObject);

          Router router = setupRouter();

          return vertx
              .createHttpServer()
              .requestHandler(router)
              .listen(serverPort)
              .onSuccess(http -> {
                System.out.println("HTTP server started on port " + serverPort);
              });
        });
  }

  private Pool getPostgresqlPool(JsonObject jsonObject) {
    JsonObject databaseConfig = jsonObject.getJsonObject("databases", new JsonObject());
    JsonObject postgresConfig = databaseConfig.getJsonObject("postgresql", new JsonObject());
    PgConnectOptions connectOptions = new PgConnectOptions()
        .setPort(postgresConfig.getInteger("port", 5432))
        .setHost(postgresConfig.getString("host", "localhost"))
        .setDatabase(postgresConfig.getString("database", "main"))
        .setUser(postgresConfig.getString("username", "user"))
        .setPassword(postgresConfig.getString("password", "password"));
    PoolOptions poolOptions = new PoolOptions()
        .setMaxSize(databaseConfig.getInteger("max-pool", 16)).setConnectionTimeout(10);
    return PgBuilder
        .pool()
        .with(poolOptions)
        .connectingTo(connectOptions)
        .using(vertx)
        .build();
  }

  private Router setupRouter() {
    Router router = Router.router(vertx);

    router.get("/").handler(rc -> {
      AppResponse<String> appResponse = AppResponse.success("Hello from Vert.x!");
      rc.response()
          .putHeader("content-type", "application/json")
          .end(appResponse.toJsonString());
    });

    router.get("/api/conditions").handler(this::getAllConditions);

    return router;
  }

  private void getAllConditions(RoutingContext routingContext) {
    if (pool == null) {
      System.out.println("pool is null");
      return;
    }

    String sql = "SELECT * FROM conditions";
    System.out.println(sql);

    pool.getConnection().compose(conn -> {
      System.out.println("Got a connection from the pool");

      return conn
          .query(sql)
          .execute()
          .onSuccess(rows -> {
            System.out.println(rows.size() + " rows retrieved.");

            List<Conditions> list = new ArrayList<>();
            for (Row row : rows) {
              list.add(Conditions.fromRow(row));
            }

            AppResponse<Object> appResponse = AppResponse.success(list);
            routingContext.response()
                .putHeader("content-type", "application/json")
                .end(appResponse.toJsonString());
          })
          .onFailure(cause -> {
            System.err.println("Database query failed: " + cause.getMessage());
            routingContext.response()
                .setStatusCode(500)
                .end(new JsonObject().put("error", "Database error").encode());
          });
    }).onComplete(ar -> {
      if (ar.succeeded()) {
        System.out.println("Done");
      } else {
        System.out.println("Something went wrong " + ar.cause().getMessage());
      }
    });
  }

}