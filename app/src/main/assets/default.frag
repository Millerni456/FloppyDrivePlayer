#version 300 es

in vec2 TexCoord;

out vec4 color;

uniform sampler2D texture;

void main()
{
    vec2 flippedTexCoord = vec2(TexCoord.x, 1.0 - TexCoord.y);
	color = texture(texture, flippedTexCoord);
}