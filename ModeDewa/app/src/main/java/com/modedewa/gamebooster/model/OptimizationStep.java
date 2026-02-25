package com.modedewa.gamebooster.model;

/**
 * OptimizationStep — Tracks progress of each optimization step.
 */
public class OptimizationStep {
    public enum Status {
        PENDING,
        RUNNING,
        SUCCESS,
        FAILED,
        SKIPPED
    }

    public String name;
    public String description;
    public String emoji;
    public Status status;
    public String message;

    /**
     * Buat step optimisasi baru dengan status PENDING.
     *
     * @param emoji emoji indikator untuk display UI
     * @param name  nama singkat step
     * @param description deskripsi apa yang dilakukan step ini
     */
    public OptimizationStep(String emoji, String name, String description) {
        this.emoji = emoji;
        this.name = name;
        this.description = description;
        this.status = Status.PENDING;
        this.message = "";
    }

    /**
     * Cek apakah step sudah selesai (berhasil, gagal, atau dilewati).
     *
     * @return true jika status adalah SUCCESS, FAILED, atau SKIPPED
     */
    public boolean isDone() {
        return status == Status.SUCCESS || status == Status.FAILED || status == Status.SKIPPED;
    }
}
