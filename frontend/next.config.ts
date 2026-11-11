import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: process.env.HANKKEUT_CONTAINER_BUILD === "1" ? "standalone" : undefined,
  reactStrictMode: true,
  experimental: {
    serverActions: { bodySizeLimit: "11mb" },
  },
};

export default nextConfig;
