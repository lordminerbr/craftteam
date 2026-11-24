package com.craftteams.client;

import com.craftteams.net.ActionPayload;
import com.craftteams.net.StatePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Tela de administracao: times na esquerda, mods liberados do time escolhido na direita. */
public class TeamScreen extends Screen {
    private List<String> mods = new ArrayList<>();
    private List<String> teams = new ArrayList<>();
    private final Map<String, Set<String>> teamMods = new HashMap<>();
    private final Map<String, List<String>> members = new HashMap<>();

    private String selectedTeam = null;
    private int page = 0;
    private String newTeamText = "";
    private String playerText = "";
    private int membersY = 0;
    private String pageLabel = "1/1";

    public TeamScreen(StatePayload payload) {
        super(Component.literal("Craft Teams"));
        apply(payload);
    }

    public void update(StatePayload payload) {
        apply(payload);
        rebuildWidgets();
    }

    private void apply(StatePayload p) {
        mods = new ArrayList<>(p.mods());
        teams = new ArrayList<>(p.teams());
        teamMods.clear();
        members.clear();
        for (String e : p.teamMods()) {
            int i = e.indexOf('|');
            if (i < 0) continue;
            teamMods.computeIfAbsent(e.substring(0, i), k -> new HashSet<>()).add(e.substring(i + 1));
        }
        for (String e : p.members()) {
            int i = e.indexOf('|');
            if (i < 0) continue;
            members.computeIfAbsent(e.substring(0, i), k -> new ArrayList<>()).add(e.substring(i + 1));
        }
        if (selectedTeam == null || !teams.contains(selectedTeam)) {
            selectedTeam = teams.isEmpty() ? null : teams.get(0);
        }
    }

    private void send(String action, String team, String arg) {
        PacketDistributor.sendToServer(new ActionPayload(action, team, arg));
    }

    private void cycleTeam(int delta) {
        if (teams.isEmpty()) return;
        int i = Math.max(0, teams.indexOf(selectedTeam));
        selectedTeam = teams.get((i + delta + teams.size()) % teams.size());
        rebuildWidgets();
    }

    @Override
    protected void init() {
        int left = 10;
        int y = 28;

        EditBox newTeam = new EditBox(this.font, left, y, 110, 20, Component.literal("novo time"));
        newTeam.setMaxLength(16);
        newTeam.setValue(newTeamText);
        newTeam.setResponder(s -> newTeamText = s);
        addRenderableWidget(newTeam);
        addRenderableWidget(Button.builder(Component.literal("Criar time"), b -> {
            if (!newTeamText.isBlank()) send("create_team", newTeamText.trim(), "");
        }).bounds(left + 115, y, 85, 20).build());

        y += 26;
        addRenderableWidget(Button.builder(Component.literal("<"), b -> cycleTeam(-1))
            .bounds(left, y, 20, 20).build());
        Button teamName = Button.builder(
            Component.literal(selectedTeam == null ? "(sem times)" : selectedTeam), b -> { })
            .bounds(left + 24, y, 152, 20).build();
        teamName.active = false;
        addRenderableWidget(teamName);
        addRenderableWidget(Button.builder(Component.literal(">"), b -> cycleTeam(1))
            .bounds(left + 180, y, 20, 20).build());

        y += 26;
        addRenderableWidget(Button.builder(Component.literal("Excluir time"), b -> {
            if (selectedTeam != null) send("delete_team", selectedTeam, "");
        }).bounds(left, y, 200, 20).build());

        y += 26;
        EditBox player = new EditBox(this.font, left, y, 110, 20, Component.literal("jogador"));
        player.setMaxLength(32);
        player.setValue(playerText);
        player.setResponder(s -> playerText = s);
        addRenderableWidget(player);
        addRenderableWidget(Button.builder(Component.literal("+"), b -> {
            if (selectedTeam != null && !playerText.isBlank()) send("add_player", selectedTeam, playerText.trim());
        }).bounds(left + 115, y, 40, 20).build());
        addRenderableWidget(Button.builder(Component.literal("-"), b -> {
            if (selectedTeam != null && !playerText.isBlank()) send("remove_player", selectedTeam, playerText.trim());
        }).bounds(left + 160, y, 40, 20).build());
        membersY = y + 28;

        int right = 220;
        int top = 28;
        int perPage = Math.max(3, (this.height - top - 30) / 22);
        int pages = Math.max(1, (mods.size() + perPage - 1) / perPage);
        page = Math.max(0, Math.min(page, pages - 1));
        int width = Math.max(100, Math.min(220, this.width - right - 10));
        final int maxPage = pages - 1;

        for (int i = 0; i < perPage; i++) {
            int idx = page * perPage + i;
            if (idx >= mods.size()) break;
            String mod = mods.get(idx);
            boolean on = selectedTeam != null
                && teamMods.getOrDefault(selectedTeam, Set.of()).contains(mod);
            Button btn = Button.builder(Component.literal((on ? "[x] " : "[ ] ") + mod), b -> {
                if (selectedTeam != null) send("toggle_mod", selectedTeam, mod);
            }).bounds(right, top + i * 22, width, 20).build();
            btn.active = selectedTeam != null;
            addRenderableWidget(btn);
        }

        int navY = this.height - 26;
        addRenderableWidget(Button.builder(Component.literal("<"), b -> {
            page = Math.max(0, page - 1);
            rebuildWidgets();
        }).bounds(right, navY, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> {
            page = Math.min(maxPage, page + 1);
            rebuildWidgets();
        }).bounds(right + width - 20, navY, 20, 20).build());
        pageLabel = (page + 1) + "/" + pages;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawString(this.font, "Times e mods liberados para craft", 10, 10, 0xFFFFFF);
        g.drawString(this.font, "Membros:", 10, membersY, 0xFFFFFF);

        List<String> m = selectedTeam == null ? List.of() : members.getOrDefault(selectedTeam, List.of());
        int yy = membersY + 12;
        for (String name : m) {
            if (yy > this.height - 10) break;
            g.drawString(this.font, name, 14, yy, 0xAAAAAA);
            yy += 10;
        }
        g.drawString(this.font, pageLabel, 220 + 26, this.height - 20, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
