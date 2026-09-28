package com.lion.villagersplus.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class ExperienceParticle extends TextureSheetParticle {

    public ExperienceParticle(ClientLevel world, SpriteSet spriteProvider, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y - 0.125D, z, velocityX, velocityY, velocityZ);
        this.setSize(0.01F, 0.01F);
        this.pickSprite(spriteProvider);
        this.quadSize *= this.random.nextFloat() * 0.6F + 0.6F;
        this.lifetime = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
        this.hasPhysics = false;
        this.friction = 1.0F;
        this.gravity = 0.0F;
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class ExperienceParticleFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public ExperienceParticleFactory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(SimpleParticleType defaultParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i) {
            ExperienceParticle waterSuspendParticle = new ExperienceParticle(clientWorld, this.spriteProvider, d, e, f, 0.0D, -0.800000011920929D, 0.0D);
            waterSuspendParticle.lifetime = Mth.randomBetweenInclusive(clientWorld.random, 500, 1000);
            waterSuspendParticle.gravity = 0.01F;
            waterSuspendParticle.setColor(0.655F, 0.8F, 0.1F);
            return waterSuspendParticle;
        }
    }
}

