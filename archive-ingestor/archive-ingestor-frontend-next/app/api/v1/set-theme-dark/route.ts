import { setThemeDark, RedisResponse } from "@/app/lib/server/redis";

export async function GET(request: Request) {
    let res: RedisResponse = {response: await setThemeDark()};

    return new Response(JSON.stringify(res), {
        status: 200,
        headers: {"Content-Type": "application/json"}
    });
}
