import { getCurrentTheme, RedisResponse } from "@/app/lib/server/redis";

export async function GET(request: Request) {
    let res: RedisResponse = {response: await getCurrentTheme()};

    return new Response(JSON.stringify(res), {
        status: 200,
        headers: {"Content-Type": "application/json"}
    });
}
