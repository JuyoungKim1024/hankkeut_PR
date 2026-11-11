import { ImageResponse } from "next/og";

export const size = { width: 32, height: 32 };
export const contentType = "image/png";

export default function Icon() {
  return new ImageResponse(
    <div style={{
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      justifyContent: "center",
      gap: 2,
      width: "100%",
      height: "100%",
      borderRadius: 8,
      background: "#d8ff3e",
    }}>
      <div style={{ width: 7, height: 2, background: "#0a0a0a" }} />
      <div style={{ width: 17, height: 3, background: "#0a0a0a" }} />
      <div style={{ width: 12, height: 10, border: "3px solid #0a0a0a", borderRadius: 10 }} />
    </div>,
    size,
  );
}
