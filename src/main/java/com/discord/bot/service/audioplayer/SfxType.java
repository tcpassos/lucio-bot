package com.discord.bot.service.audioplayer;

import java.util.List;

public enum SfxType {

    MUSIC_START(List.of("fazer_barulho.ogg", "solta_batida.ogg", "querer_ouvir.ogg")),
    VOLUME_UP(List.of("aumentando_ganho.ogg", "aumentar_o_som.ogg", "mais_alto.ogg", "pra_cima.ogg")),
    VOLUME_LOW(List.of("nao_to_te_ouvindo.ogg"));

    private final List<String> sfxList;

    SfxType(List<String> sfxList) {
        this.sfxList = sfxList;
    }

    public List<String> getSfxList() {
        return sfxList;
    }

}
