package net.neganote.monilabs.mixin.blackhole;

import net.minecraft.resources.ResourceLocation;
import net.neganote.monilabs.client.render.BlackHoleRendererHelpers;

import me.jellysquid.mods.sodium.client.gl.shader.ShaderLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ShaderLoader.class, remap = false)
public class ShaderLoaderMixin {

    @Inject(method = "getShaderSource", at = @At("RETURN"), cancellable = true, remap = false)
    private static void monilabs$interceptGetShaderSource(ResourceLocation name,
                                                          CallbackInfoReturnable<String> cir) {
        if (BlackHoleRendererHelpers.isTranslucentShader) {
            if (name.toString().endsWith(".fsh")) {
                String originalSource = cir.getReturnValue();
                String myUniform = "\nuniform sampler2D u_BlackHoleDepthTexture;\nuniform int uDrawInFrontOfBlackHole;\nuniform int uAnyBlackHoles = 0;\n";
                String regex = "(?s)(.*)(uniform sampler2D\\s+\\w+;)([^\r\n]*)";

                var replaced = originalSource.replaceFirst(regex, "$1$2$3\n" + myUniform)
                        .replace("void main() {",
                                """
                                        void main() {
                                         float sphereDepth = texture(u_BlackHoleDepthTexture, gl_FragCoord.xy / vec2(textureSize(u_BlackHoleDepthTexture, 0))).r;
                                         bool isBehindBlackHole = gl_FragCoord.z >= sphereDepth;
                                         if (uAnyBlackHoles == 1 && uDrawInFrontOfBlackHole == 0 && !isBehindBlackHole && sphereDepth < 1.0)
                                            discard;
                                         if (uAnyBlackHoles == 1 && uDrawInFrontOfBlackHole == 1 && (sphereDepth >= 1.0 || isBehindBlackHole))
                                            discard;
                                        """);
                cir.setReturnValue(replaced);
            }
        }
    }
}
