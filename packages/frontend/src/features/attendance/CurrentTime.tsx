"use client";

import { useEffect, useRef } from "react";

export function CurrentTime() {
  const timeRef = useRef<HTMLParagraphElement>(null);
  const dateRef = useRef<HTMLParagraphElement>(null);

  useEffect(() => {
    function update() {
      const now = new Date();
      if (timeRef.current) {
        timeRef.current.textContent = now.toLocaleTimeString("ja-JP", {
          timeZone: "Asia/Tokyo",
          hour: "2-digit",
          minute: "2-digit",
          second: "2-digit",
          hour12: false,
        });
      }
      if (dateRef.current) {
        dateRef.current.textContent = now.toLocaleDateString("ja-JP", {
          timeZone: "Asia/Tokyo",
          year: "numeric",
          month: "long",
          day: "numeric",
          weekday: "long",
        });
      }
    }
    update();
    const id = setInterval(update, 1000);
    return () => clearInterval(id);
  }, []);

  return (
    <div className="text-center">
      <p ref={timeRef} className="text-4xl font-bold tabular-nums tracking-tight" />
      <p ref={dateRef} className="text-sm text-muted-foreground mt-1" />
    </div>
  );
}
