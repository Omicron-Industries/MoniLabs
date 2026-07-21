package net.neganote.monilabs.common.gui.widget;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.network.FriendlyByteBuf;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class IndexedSelectorWidget extends SelectorWidget {

    private Consumer<String> onChangedCopy;
    private Supplier<String> supplierCopy;

    public IndexedSelectorWidget(int x, int y, int width, int height, List<String> candidates, int fontColor) {
        super(x, y, width, height, candidates, fontColor);
    }

    @Override
    public SelectorWidget setOnChanged(Consumer<String> onChanged) {
        this.onChangedCopy = onChanged;
        return super.setOnChanged(onChanged);
    }

    @Override
    public SelectorWidget setSupplier(Supplier<String> supplier) {
        this.supplierCopy = supplier;
        return super.setSupplier(supplier);
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        if (supplierCopy != null) {
            setValue(supplierCopy.get());
        }
    }

    @Override
    protected void computeLayout() {
        int height = Math.min(maxCount, candidates.size()) * 15;
        popUp.clearAllWidgets();
        selectables.clear();
        popUp.setSize(new Size(getSize().width, height));
        popUp.setSelfPosition(showUp ? new Position(0, -height) : new Position(0, getSize().height));
        if (candidates.size() > maxCount) {
            popUp.setYScrollBarWidth(4).setYBarStyle(null, new ColorRectTexture(-1));
        }
        int y = 0;
        int width = candidates.size() > maxCount ? getSize().width - 4 : getSize().width;
        for (int i = 0; i < candidates.size(); i++) {
            String candidate = candidates.get(i);
            int idx = i;
            SelectableWidgetGroup select = new SelectableWidgetGroup(0, y, width, 15);
            select.addWidget(new ImageWidget(0, 0, width, 15,
                    new TextTexture(candidate, fontColor).setWidth(width).setType(TextTexture.TextType.ROLL)));
            select.setSelectedTexture(-1, -1);
            select.setOnSelected(s -> {
                setValue(candidate);
                if (onChangedCopy != null) {
                    onChangedCopy.accept(candidate);
                }
                writeClientAction(2, buffer -> buffer.writeVarInt(idx));
                setShow(false);
            });
            popUp.addWidget(select);
            selectables.add(select);
            y += 15;
        }
        popUp.setScrollYOffset(0);
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        if (id == 2) {
            int index = buffer.readVarInt();
            if (index >= 0 && index < candidates.size()) {
                String value = candidates.get(index);
                setValue(value);
                if (onChangedCopy != null) {
                    onChangedCopy.accept(value);
                }
            }
        } else {
            super.handleClientAction(id, buffer);
        }
    }
}
