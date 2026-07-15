package net.neganote.monilabs.mixin;

import net.neganote.monilabs.MoniLabs;
import net.neganote.monilabs.client.render.ShaderAnalysisResult;

import com.mojang.blaze3d.shaders.Program;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(Program.class)
public class ProgramMixin {

    @Unique
    private static final Pattern MAIN_PATTERN = Pattern.compile("void\\s+main\\s*\\(\\s*\\)\\s*\\{([\\s\\S]*)}");
    @Unique
    private static final Pattern DECL_PATTERN = Pattern
            .compile("\\b((?:[iu]?vec\\d|float|int|bool))\\b\\s+([\\w\\d_]+)");
    @Unique
    private static final Pattern TARGET_PATTERN = Pattern
            .compile("layout\\s*\\(\\s*location\\s*=\\s*0\\s*\\)\\s*out\\s+vec4\\s+(\\w+)");
    @Unique
    private static final Pattern VAR_PATTERN = Pattern.compile("\\b([a-zA-Z_]\\w*)\\b");

    // Works for all popular shaders
    // Basically what this does is find the last write to the color variable before it gets actually written to the
    // framebuffer
    @Unique
    private static ShaderAnalysisResult moniLabs$analyzeShaderForColorWrite(String content) {
        Matcher mainMatcher = MAIN_PATTERN.matcher(content);
        if (!mainMatcher.find()) return new ShaderAnalysisResult("", "", false);

        String mainBody = mainMatcher.group(1);

        Matcher targetMatcher = TARGET_PATTERN.matcher(content);
        String targetOut = targetMatcher.find() ? targetMatcher.group(1) : "gl_FragColor";

        Map<String, String> knownTypes = new HashMap<>();
        String[] lines = mainBody.split("\n");

        for (String line : lines) {
            line = line.trim().replaceAll("//.*", "");
            if (line.isEmpty()) {
                continue;
            }

            Matcher declMatch = DECL_PATTERN.matcher(line);
            while (declMatch.find()) {
                knownTypes.put(declMatch.group(2), declMatch.group(1));
            }

            if (line.contains(targetOut) && line.contains("=")) {
                String rightSide = line.substring(line.indexOf('=') + 1);
                Matcher varMatch = VAR_PATTERN.matcher(rightSide);

                while (varMatch.find()) {
                    String candidate = varMatch.group(1);
                    if (Character.isDigit(candidate.charAt(0))) continue;

                    if (knownTypes.containsKey(candidate)) {
                        return new ShaderAnalysisResult(line, candidate, true);
                    }
                }
            }
        }
        return new ShaderAnalysisResult("", "", false);
    }

    @Unique
    private static String moniLabs$modifyIrisShaderpackShader(String code, ShaderAnalysisResult result,
                                                              Program.Type type) {
        if (type == Program.Type.VERTEX) {
            return code.replace("void main() {", """
                    in uvec4 Colors;
                    in vec4 ParticleSpeeds;
                    flat out uvec4 vColors;
                    flat out vec4 vParticleSpeeds;

                    void main () {
                    vColors = Colors;
                    vParticleSpeeds = ParticleSpeeds;""");
        }
        if (!result.valid()) {
            MoniLabs.LOGGER.log(Level.ERROR,
                    "Could not successfully analyze current shader for colored ender portal inject");
            return code;
        }
        String newSource = code.replace("void main", """

                const vec3[] COLORS = vec3[](
                vec3(0.022087, 0.098399, 0.110818),
                vec3(0.011892, 0.095924, 0.089485),
                vec3(0.027636, 0.101689, 0.100326),
                vec3(0.046564, 0.109883, 0.114838),
                vec3(0.064901, 0.117696, 0.097189),
                vec3(0.063761, 0.086895, 0.123646),
                vec3(0.084817, 0.111994, 0.166380),
                vec3(0.097489, 0.154120, 0.091064),
                vec3(0.106152, 0.131144, 0.195191),
                vec3(0.097721, 0.110188, 0.187229),
                vec3(0.133516, 0.138278, 0.148582),
                vec3(0.070006, 0.243332, 0.235792),
                vec3(0.196766, 0.142899, 0.214696),
                vec3(0.047281, 0.315338, 0.321970),
                vec3(0.204675, 0.390010, 0.302066),
                vec3(0.080955, 0.314821, 0.661491)
                );

                const mat4 SCALE_TRANSLATE = mat4(
                0.5, 0.0, 0.0, 0.25,
                0.0, 0.5, 0.0, 0.25,
                0.0, 0.0, 1.0, 0.0,
                0.0, 0.0, 0.0, 1.0
                );

                mat2 mat2_rotate_z(float radians) {
                    return mat2(
                    cos(radians), -sin(radians),
                    sin(radians), cos(radians)
                    );
                }

                vec3 moni_rgb2hsv(vec3 c) {
                    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
                    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
                    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

                    float d = q.x - min(q.w, q.y);
                    float e = 1.0e-10;
                    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
                }
                vec3 moni_hsv2rgb(vec3 c) {
                    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
                    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
                    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
                }
                void unpackPalette(uvec4 packedColors, out vec4[4] palette) {
                    for (int i = 0; i < 4; i++) {
                        uint c = packedColors[i];
                        palette[i] = vec4(
                            float((c >> 16) & 0xFFu) / 255.0, //Red
                            float((c >> 8) & 0xFFu) / 255.0,  //Green
                            float((c >> 0) & 0xFFu) / 255.0,  //Blue
                            1.0 //Alpha
                        );
                    }
                }

                void expandPalette(vec4[4] base4, out vec4[16] palette) {
                    for (int i = 0; i < 4; i++) {
                        vec3 rgb = base4[i].rgb;
                        vec3 hsv = moni_rgb2hsv(rgb);

                        int baseIdx = i * 4;

                        //Original
                        palette[baseIdx] = base4[i];

                        //Brighter
                        palette[baseIdx + 1] = vec4(moni_hsv2rgb(vec3(
                        hsv.x,
                        clamp(hsv.y - 0.05, 0.0, 1.0),
                        clamp(hsv.z + 0.15, 0.0, 1.0)
                        )), 1.0);

                        //Darker
                        palette[baseIdx + 2] = vec4(moni_hsv2rgb(vec3(
                        hsv.x,
                        clamp(hsv.y + 0.1, 0.0, 1.0),
                        clamp(hsv.z - 0.2, 0.0, 1.0)
                        )), 1.0);

                        //Hue shifted
                        palette[baseIdx + 3] = vec4(moni_hsv2rgb(vec3(
                        fract(hsv.x + 0.08),
                        hsv.y,
                        hsv.z
                        )), 1.0);
                    }
                }


                flat in vec4 vParticleSpeeds;
                flat in uvec4 vColors;
                uniform sampler2D _EndPortalTexture;
                uniform sampler2D _EndSkyTexture;
                #PLACEHOLDER_VIEWWIDTH
                #PLACEHOLDER_VIEWHEIGHT
                #PLACEHOLDER_FRAMETIMECOUNTER
                void main""");
        newSource = newSource.replace("#PLACEHOLDER_VIEWWIDTH",
                newSource.contains("viewWidth") ? "" : "uniform float viewWidth;");
        newSource = newSource.replace("#PLACEHOLDER_VIEWHEIGHT",
                newSource.contains("viewHeight") ? "" : "uniform float viewHeight;");
        newSource = newSource.replace("#PLACEHOLDER_FRAMETIMECOUNTER",
                newSource.contains("frameTimeCounter") ? "" : "uniform float frameTimeCounter;");
        // we do this to make sure that all referenced uniforms are in the source code before they are used
        newSource = newSource.replace("void main", """
                 mat4 end_portal_layer(float layer, float speedModifier) {
                      mat4 translate = mat4(
                      1.0, 0.0, 0.0, 17.0 / layer,
                      0.0, 1.0, 0.0, (2.0 + layer / 1.5) * (float(frameTimeCounter) / 800 * speedModifier),
                      0.0, 0.0, 1.0, 0.0,
                      0.0, 0.0, 0.0, 1.0
                      );

                      mat2 rotate = mat2_rotate_z(radians((layer * layer * 4321.0 + layer * 9.0) * 2.0));

                      mat2 scale = mat2((4.5 - layer / 4.0) * 2.0);

                      return mat4(scale * rotate) * translate * SCALE_TRANSLATE;
                  }
                void main""");
        newSource = newSource.replace(result.firstWriteLine(),
                """
                        if (iris_entityInfo.x == 6767) {
                            float _speeds[4] = float[](vParticleSpeeds.x, vParticleSpeeds.y, vParticleSpeeds.z, vParticleSpeeds.w);
                            vec4 _palette[4];
                            vec4 _expandedPalette[16];
                            unpackPalette(vColors, _palette);
                            expandPalette(_palette, _expandedPalette);
                            vec2 screenUV = gl_FragCoord.xy / vec2(viewWidth, viewHeight);

                            vec3 _color = texture(_EndSkyTexture, screenUV).rgb * iris_vertexColor.rgb;
                            for (int i = 0; i < 16; i++) {
                                mat4 layer = end_portal_layer(float(i + 1), _speeds[i / 4]);
                                vec4 transformed = vec4(screenUV, 1.0, 1.0) * layer;
                                vec2 finalTexCoord = transformed.xy / transformed.w;
                                vec3 textureColor = texture(_EndPortalTexture, finalTexCoord).rgb;
                                bool isStar = length(textureColor) > 0.1;
                                bool isBlackish = length(_expandedPalette[i].rgb) < 0.1;
                                if (isBlackish && isStar)
                                    _color = _expandedPalette[i].rgb;
                                else
                                    _color += textureColor * _expandedPalette[i].rgb;
                            }

                            %s.rgb = _color;
                        }
                        """
                        .formatted(result.referencedVariable()) + "\n\t" +
                        result.firstWriteLine());
        return newSource;
    }

    @Redirect(method = "compileShaderInternal",
              at = @At(value = "INVOKE",
                       target = "Lorg/apache/commons/io/IOUtils;toString(Ljava/io/InputStream;Ljava/nio/charset/Charset;)Ljava/lang/String;"))
    private static String moniLabs$modifyIrisEntityDiffuseShader(InputStream sw, Charset input, Program.Type type,
                                                                 String name) throws IOException {
        String original = IOUtils.toString(sw, input);
        if (name.contains("block_entity_diffuse")) {
            var analysisResult = moniLabs$analyzeShaderForColorWrite(original);
            return moniLabs$modifyIrisShaderpackShader(original, analysisResult, type);
        }

        return original;
    }
}
