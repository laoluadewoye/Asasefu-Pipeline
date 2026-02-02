import { createClient } from "redis";

const redisClient = await createClient().on(
    "error", (err) => console.log("Redis Client Error", err)
).connect();

export interface RedisResponse {
    response: string | null;
}

export async function setThemeDark(): Promise<string | null> {
    let result: string | null = await redisClient.SET("theme", "dark");
    return result;
}

export async function setThemeLight(): Promise<string | null> {
    let result: string | null = await redisClient.SET("theme", "light");
    return result;
}

export async function getCurrentTheme(): Promise<string | null> {
    let result: string | null = await redisClient.GET("theme");
    return result;
}
