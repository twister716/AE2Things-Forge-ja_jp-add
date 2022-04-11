package io.github.projectet.ae2things.block;

import javax.annotation.Nullable;

import io.github.projectet.ae2things.AE2Things;
import io.github.projectet.ae2things.block.entity.BEAdvancedInscriber;
import io.github.projectet.ae2things.gui.advancedInscriber.AdvancedInscriberMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import appeng.block.AEBaseEntityBlock;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;

public class BlockAdvancedInscriber extends AEBaseEntityBlock<BEAdvancedInscriber> {

    public BlockAdvancedInscriber(BlockBehaviour.Properties settings) {
        super(settings);
        settings.requiresCorrectToolForDrops();
        this.registerDefaultState(this.defaultBlockState().setValue(SMASHING, false));
    }

    public static final BooleanProperty SMASHING = BooleanProperty.create("smashing");

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, BEAdvancedInscriber be) {
        return currentState.setValue(SMASHING, be.isSmash());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SMASHING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return AE2Things.ADVANCED_INSCRIBER_BE.get().create(pos, state);
    }

    @Override
    public InteractionResult onActivated(final Level level, final BlockPos pos, final Player p,
            final InteractionHand hand,
            final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        if (!InteractionUtil.isInAlternateUseMode(p)) {
            final BEAdvancedInscriber ai = (BEAdvancedInscriber) level.getBlockEntity(pos);
            if (ai != null) {
                if (!level.isClientSide()) {
                    MenuOpener.open(AdvancedInscriberMenu.ADVANCED_INSCRIBER_SHT, p,
                            MenuLocators.forBlockEntity(ai));
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return InteractionResult.PASS;
    }

}
