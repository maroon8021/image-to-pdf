import fastify from "fastify";
import fastifyProxy from "@fastify/http-proxy";

const server = fastify();

// frontend
server.register(fastifyProxy, {
  upstream: "http://localhost:1234",
});

// backend
server.register(fastifyProxy, {
  upstream: "http://localhost:51111",
  prefix: "/api",
  rewritePrefix: "/api",
});

server.listen({ port: 50001 });
