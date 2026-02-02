import { setThemeLight, RedisResponse } from "@/app/lib/server/redis";

export async function GET(request: Request) {
    let res: RedisResponse = {response: await setThemeLight()};

    return new Response(JSON.stringify(res), {
        status: 200,
        headers: {"Content-Type": "application/json"}
    });
}
